package com.tc.modules.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tc.common.http.DataResponse;
import com.tc.common.http.ErrorCode;
import com.tc.common.utils.HttpUtils;
import com.tc.modules.entity.TAudioInfo;
import com.tc.modules.entity.TCameraInfo;
import com.tc.modules.entity.THardware;
import com.tc.modules.entity.TSite;
import com.tc.modules.query.CommonQuery;
import com.tc.modules.service.TAudioInfoService;
import com.tc.modules.service.TAudioService;
import com.tc.modules.service.THardwareService;
import com.tc.modules.service.TSiteService;
import com.tc.modules.vo.AudioResponse;
import com.tc.modules.vo.AudioVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AudioServiceImpl implements TAudioService {

    @Value("${audio.url}")
    private String audioUrl;

    @Value("${audio.api-key}")
    private String audioApiKey;

    @Value("${audio.api-secret}")
    private String audioApiSecret;

    @Autowired
    private TAudioInfoService audioInfoService;

    @Autowired
    private THardwareService hardwareService;

    @Autowired
    private TSiteService siteService;

    @Override
    public DataResponse<?> getAudioList(CommonQuery query, Integer pageNo, Integer pageSize) {

        TAudioInfo audioInfo = audioInfoService.getOne(new LambdaQueryWrapper<TAudioInfo>().eq(TAudioInfo::getHardwareId, query.getHardwareId()));
        if (audioInfo == null) {
            return DataResponse.fail(400, "音配配置信息缺失 ！", null);
        }
        THardware hardware = hardwareService.getById(query.getHardwareId());
        TSite site = siteService.getById(hardware.getSiteId());

        // 创建 SimpleDateFormat 对象，用于格式化时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        // 设置时区为 UTC
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String startTime = sdf.format(query.getStartTime());
        String endTime = sdf.format(query.getEndTime());

        // 获取监控记录
        String url = audioUrl + "/api/v1/monitor/channel/list";
        String params = "?deviceNumber=" + audioInfo.getDeviceNumber()
                + "&detectPosition=" + audioInfo.getDetectPosition()
                + "&startTime=" + startTime
                + "&endTime=" + endTime
                + "&pageNumber=" + pageNo
                + "&pageSize=" + pageSize;

        Map<String,String> headers = new HashMap<>();
        headers.put(audioApiKey, audioApiSecret);

        ResponseEntity<String> stringResponseEntity = HttpUtils.sendGet(url + params, headers, String.class);
        String body = stringResponseEntity.getBody();
        JSONObject jsonObject = JSON.parseObject(body);

        log.info("获取监控记录: {}", JSON.parseObject(body));

        JSONArray jsonArray = jsonObject.getJSONArray("list");
        String total = jsonObject.getString("total");

        List<AudioVO> voList = new ArrayList<>();
        List<AudioResponse> audioResponses = JSON.parseArray(jsonArray.toJSONString(), AudioResponse.class);
        for (AudioResponse audio : audioResponses) {
            AudioVO audioListVO = new AudioVO();
            audioListVO.setFileID(audio.getFileID());
            audioListVO.setSiteId(query.getSiteId());
            audioListVO.setSiteName(site.getSiteName());
            audioListVO.setMonitorLocation(hardware.getMonitorLocation());
            audioListVO.setMonitorInfo(Math.round(audio.getDuration()) + "秒音频");
            audioListVO.setStartTime(audio.getStartTime());
            audioListVO.setEndTime(audio.getEndTime());
            audioListVO.setTime(audio.getStartTime());
            voList.add(audioListVO);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", total);
        resultMap.put("records", voList);
        return DataResponse.success(resultMap);
    }


    @Override
    public DataResponse<JSONObject> getRealStream(CommonQuery query) {
        TAudioInfo audioInfo = audioInfoService.getOne(new LambdaQueryWrapper<TAudioInfo>().eq(TAudioInfo::getHardwareId, query.getHardwareId()));

        if (audioInfo == null) {
            return DataResponse.fail(ErrorCode.E3022.getCode(), ErrorCode.E3022.getMessage(), null);
        }

        Map<String,String> headers = new HashMap<>();
        headers.put(audioApiKey, audioApiSecret);

        String url = audioUrl + "/api/v1/audio/live";
        String params = "?deviceNumber=" + audioInfo.getDeviceNumber()
                + "&detectPosition=" + audioInfo.getDetectPosition();

        ResponseEntity<String> stringResponseEntity = HttpUtils.sendGet(url + params, headers, String.class);
        String body = stringResponseEntity.getBody();
        JSONObject jsonObject = JSON.parseObject(body);
        return DataResponse.success(jsonObject);
    }

    @Override
    public DataResponse getRealStreamByType(Integer orgId, Integer monitorType) {
        List<TAudioInfo> audioInfoList = audioInfoService.getTAudioInfoByOrgIdAndMonitorType(orgId, monitorType);
        Map<String,String> headers = new HashMap<>();
        headers.put(audioApiKey, audioApiSecret);

        List<JSONObject> jsonObjectList = new ArrayList<>();
        for (TAudioInfo audioInfo : audioInfoList) {
            String url = audioUrl + "/api/v1/audio/live";
            String params = "?deviceNumber=" + audioInfo.getDeviceNumber()
                    + "&detectPosition=" + audioInfo.getDetectPosition();
            ResponseEntity<String> stringResponseEntity = HttpUtils.sendGet(url + params, headers, String.class);
            String body = stringResponseEntity.getBody();
            JSONObject jsonObject = JSON.parseObject(body);
            jsonObject.put("siteName", audioInfo.getSiteName());
            jsonObject.put("siteId", audioInfo.getSiteId());
            jsonObject.put("hardware", audioInfo.getHardwareId());
            jsonObjectList.add(jsonObject);
        }
        return DataResponse.success(jsonObjectList) ;
    }

    @Override
    public DataResponse<?> downloadFile(String fileID) {
        Map<String,String> headers = new HashMap<>();
        headers.put(audioApiKey, audioApiSecret);
        String url = audioUrl + "/api/v1/file/download/single" + "?fileID=" + fileID;

        // 发送 GET 请求，获取响应的 InputStream
        ResponseEntity<byte[]> responseEntity = HttpUtils.sendGet(url, headers, byte[].class);
        // 获取文件内容（字节数组）
        byte[] fileBytes = responseEntity.getBody();
        // 从响应头中获取文件名
        String contentDisposition = responseEntity.getHeaders().getFirst("Content-Disposition");
        String fileName = getFileNameFromContentDisposition(contentDisposition);

        // 指定保存文件的路径
        String filePath = System.getProperty("user.dir") + "//file//" + fileName;

        // 写入文件
        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            if (fileBytes != null) {
                outputStream.write(fileBytes);
            }
            log.info("文件已成功下载到: {}", filePath);
        } catch (IOException e) {
            log.error("文件下载失败", e);
        }
        return DataResponse.success();
    }

    // 从 Content-Disposition 中提取文件名
    private static String getFileNameFromContentDisposition(String contentDisposition) {
        if (contentDisposition != null && contentDisposition.contains("attachment")) {
            // 使用正则表达式从 Content-Disposition 中提取文件名
            Pattern pattern = Pattern.compile("filename=\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(contentDisposition);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }
}
