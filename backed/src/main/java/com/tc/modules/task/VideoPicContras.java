package com.tc.modules.task;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tc.common.enums.AlarmType;
import com.tc.common.enums.HardwareType;
import com.tc.common.utils.HttpUtils;
import com.tc.modules.api.HkCameraApi;
import com.tc.modules.entity.TCameraInfo;
import com.tc.modules.entity.TConfigRule;
import com.tc.modules.entity.THardware;
import com.tc.modules.entity.TSite;
import com.tc.modules.service.*;
import com.tc.modules.vo.DiffResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
@Profile("prod") // 非 dev 环境才加载
public class VideoPicContras {

    private static final String videoStreamTemplate = "rtsp://{username}:{password}@{ip}:{port}/Streaming/Channels/{channelId}/";

    @Value("${iot.host}")
    private String host;

    @Value("${iot.port}")
    private String port;

    @Value("${iot.api-key}")
    private String apiKey;

    @Value("${iot.api-secret}")
    private String apiSecret;

    @Value("${python.alias}")
    private String pythonAlias;

    @Value("${python.script}")
    private String pythonScript;

    @Value("${file.video.folder}")
    private String videoFolder;

    @Value("${file.image.folder}")
    private String imageFolder;

    @Autowired
    private TCameraInfoService cameraInfoService;

    @Autowired
    private TSiteService siteService;

    @Autowired
    private TAlarmService alarmService;

    @Autowired
    private THardwareService hardwareService;

    @Autowired
    private TConfigRuleService configRuleService;

    @Autowired
    @Qualifier("pythonAnalysePoolTask")
    private ThreadPoolTaskExecutor pythonAnalysePoolTask;

    @Autowired
    private HkCameraApi hkCameraApi;

    private final Map<Integer, Map<Integer, Double>> ruleMap = new ConcurrentHashMap<>();


    //    @Scheduled(fixedDelay = 5000)   Linux 环境存在问题
    private void videoPicContras() {

        String url = "http://" + host + ":" + port + "/camera/capturePicture";

        Map<String, String> params = new HashMap<>();
        params.put("deviceId", "19");

        Map<String, String> headers = new HashMap<>();
        headers.put(apiKey, apiSecret);

        ResponseEntity<String> stringResponseEntity = HttpUtils.sendPostForm(url, headers, params, String.class);
        if (stringResponseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = stringResponseEntity.getBody();
            String currentDir = System.getProperty("user.dir");
            String baseImage = currentDir + File.separator + "file" + File.separator + "baseImage" + File.separator + "camera_1.jpg";

            JSONObject jsonObject = JSON.parseObject(responseBody);
            if (jsonObject.getInteger("code") == 200) {
                String filePath = jsonObject.getString("result");
//                videoAnalysis(baseImage, filePath);
            } else {
                log.error("请求失败，状态码：{}，message：{}", jsonObject.getInteger("code"), jsonObject.getString("message"));
            }
        } else {
            log.error("http 请求失败，状态码：{}", stringResponseEntity.getStatusCodeValue());
        }
    }


    /**
     * 定时任务，定期执行视频图像对比操作
     */
    @Scheduled(fixedDelay = 5000)
    private void videoPicContras2() {
        log.info("----------------------------------视频图像对比--------------------------------------------");
        // 获取所有站点列表，以便后续对每个站点的摄像头进行图像对比
        List<TSite> siteList = siteService.list();
        for (TSite tSite : siteList) {
            pythonAnalysePoolTask.execute(() -> {
                // 获取站点下的硬件摄像头信息
                List<TCameraInfo> hardwareCameraInfo = cameraInfoService.getHardwareCameraInfo(tSite.getId(), HardwareType.VIDEO.getCode());

                for (TCameraInfo cameraInfo : hardwareCameraInfo) {
                    // 构建摄像头的RTSP URL
                    String rtspUrl = videoStreamTemplate.replace("{username}", cameraInfo.getUsername())
                            .replace("{password}", cameraInfo.getPassword())
                            .replace("{ip}", cameraInfo.getIp())
                            .replace("{port}", cameraInfo.getPort())
                            .replace("{channelId}", cameraInfo.getChannelId().toString());

                    // 构建输出图像的文件路径，使用当前时间确保文件名唯一
                    String outputImage = imageFolder + File.separator + "diff" +
                            File.separator + "camera" + cameraInfo.getId() + "_" +
                            DateUtil.format(new Date(), "yyyyMMddHHmmss") + ".jpg";

                    // FFmpeg 截图命令，从视频流中截取一帧图像并保存为文件
                    String command = String.format(
                            "ffmpeg -rtsp_transport tcp -i %s -frames:v 1 -q:v 2 %s",
                            rtspUrl, outputImage
                    );

                    // 执行FFmpeg命令
                    RuntimeUtil.execForStr(command);

                    // 调用视频分析方法，对比基础图像和截取的图像
                    videoAnalysis(tSite.getId(), cameraInfo.getHardwareId(), cameraInfo.getBaseImage(), outputImage);
                }
            });
        }
    }


    /**
     * 执行视频分析任务
     * 该方法调用视频分析脚本，比较基础图像和当前图像的差异，并根据差异值和预设规则生成警报
     *
     * @param siteId       风机ID，用于标识不同的风机站点
     * @param hardwareId   设备ID，用于标识特定的监控设备
     * @param baseImage    基础图像路径，作为对比的标准图像
     * @param currentImage 当前图像路径，与基础图像进行对比的图像
     */
    private void videoAnalysis(int siteId, int hardwareId, String baseImage, String currentImage) {
        // 执行视频分析脚本
        String jsonStr = RuntimeUtil.execForStr(pythonAlias, pythonScript + File.separator + "videoAnalysisMain.py", baseImage, currentImage);
        DiffResultVo diffResult = JSON.parseObject(jsonStr.trim(), DiffResultVo.class);

        boolean result = diffResult.isResult();
        double diffValue = diffResult.getDiffValue();

        log.info("视频对比分析，风机：{}，设备：{}，结果：{}，差异值：{}", siteId, hardwareId, result, diffValue);

        Date alarmTime = new Date();

        if (result) {
            // 获取设备信息
            THardware hardware = hardwareService.getById(hardwareId);
            Integer monitorType = hardware.getMonitorType();

            // 获取与设备监控类型匹配的所有规则，并按报警级别排序
            List<TConfigRule> ruleList = configRuleService.list(new LambdaQueryWrapper<TConfigRule>().eq(TConfigRule::getMonitorType, monitorType));
            ruleList.sort(Comparator.comparing(TConfigRule::getAlarmLevel));

            // 根据差异值获取匹配的规则
            TConfigRule matchingRule = getMatchingRule(ruleList, diffValue);
            Map<Integer, Double> integerDoubleMap = ruleMap.get(siteId);

            // 如果没有匹配到规则，记录警告日志
            if (matchingRule == null) {
                log.warn("视频对比分析，没有匹配到规则 !!!");
                FileUtil.del(currentImage);
                return;
            }

            String fileName = videoFolder + File.separator +
                    hardwareId + "_" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + ".mp4";

            Map<String, String> map = new HashMap<>();
            map.put("alarmImage", currentImage);
            map.put("alarmVideo", fileName);


            // 如果当前站点ID在规则映射中不存在，初始化并记录本次分析结果
            if (integerDoubleMap == null) {
                ruleMap.computeIfAbsent(siteId, k -> new ConcurrentHashMap<>()).put(hardwareId, diffValue);
                alarmService.addAlarmInfo(siteId, hardwareId + "", AlarmType.VIDEO_ALARM, matchingRule, alarmTime, JSON.toJSONString(map));
                saveVideoFile(hardwareId, fileName, alarmTime);
                log.info("视频图像对比结果：{}, 阈值：{}, 匹配规则：{}", result, diffValue, matchingRule);
                return;
            }

            // 如果当前设备ID在站点映射中不存在，记录本次分析结果
            if (!integerDoubleMap.containsKey(hardwareId)) {
                ruleMap.computeIfAbsent(siteId, k -> new ConcurrentHashMap<>()).put(hardwareId, diffValue);
                alarmService.addAlarmInfo(siteId, hardwareId + "", AlarmType.VIDEO_ALARM, matchingRule, alarmTime, JSON.toJSONString(map));
                saveVideoFile(hardwareId, fileName, alarmTime);
                log.info("视频图像对比结果：{}, 阈值：{}, 匹配规则：{}", result, diffValue, matchingRule);
                return;
            }

            // 获取上次的差异值，并更新本次的差异值
            Double value = integerDoubleMap.get(hardwareId);
            ruleMap.computeIfAbsent(siteId, k -> new ConcurrentHashMap<>()).put(hardwareId, diffValue);

            // 判断本次告警与上次的差异是否足够大，如果大于阈值，则生成新的警报
            if (Math.abs(diffValue - value) > 5) {
                alarmService.addAlarmInfo(siteId, hardwareId + "", AlarmType.VIDEO_ALARM, matchingRule, alarmTime, JSON.toJSONString(map));
                log.info("视频图像对比结果：{}, 阈值：{}, 匹配规则：{}", result, diffValue, matchingRule);
            } else {
                FileUtil.del(currentImage);
            }
        } else {
            FileUtil.del(currentImage);
        }
    }


    public TConfigRule getMatchingRule(List<TConfigRule> ruleList, double value) {
        // 按报警级别降序排列（一级 > 二级 > 三级）
        ruleList.sort(Comparator.comparing(TConfigRule::getAlarmLevel));

        for (TConfigRule rule : ruleList) {
            double threshold = Double.parseDouble(rule.getThreshold());
            String symbol = rule.getSymbol();

            // 根据符号进行比较
            if (compare(value, threshold, symbol)) {
                return rule; // 返回匹配的规则对象
            }
        }
        return null; // 没有匹配的规则
    }


    private static boolean compare(double value, double threshold, String symbol) {
        switch (symbol) {
            case ">":
                return value > threshold;
            case ">=":
                return value >= threshold;
            case "<":
                return value < threshold;
            case "<=":
                return value <= threshold;
            case "==":
                return value == threshold;
            case "!=":
                return value != threshold;
            default:
                throw new IllegalArgumentException("Unsupported comparison symbol: " + symbol);
        }
    }

    private void saveVideoFile(int hardwareId, String fileName, Date alarmTime) {
        DateTime startTime = DateUtil.offset(alarmTime, DateField.MINUTE, -10);
        DateTime endTime = DateUtil.offset(alarmTime, DateField.MINUTE, 10);
        hkCameraApi.downloadRecordByTime(hardwareId, startTime, endTime, fileName);
    }
}
