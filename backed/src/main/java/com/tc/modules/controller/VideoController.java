package com.tc.modules.controller;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tc.common.http.DataResponse;
import com.tc.common.http.ErrorCode;
import com.tc.common.utils.RTSPEncryptor;
import com.tc.common.utils.TimeSplitter;
import com.tc.modules.entity.*;
import com.tc.modules.service.*;
import com.tc.modules.vo.VideoDataVo;
import com.tc.modules.vo.VideoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/video")
@Api(tags = "视频管理")
public class VideoController {

    private static final String videoStreamTemplate = "rtsp://{username}:{password}@{ip}:{port}/Streaming/Channels/{channelId}/";
    private static final String videoStreamHisTemplate = "rtsp://{username}:{password}@{ip}:{port}/Streaming/tracks/{channelId}";

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd't'HHmmss'z'");

    @Autowired
    private THardwareService hardwareService;

    @Autowired
    private TSiteService siteService;

    @Autowired
    private TCameraInfoService cameraInfoService;

    @Autowired
    private TNvrInfoService nvrInfoService;

    @Resource
    private TSysConfigService sysConfigService;


    @ApiOperation("获取视频列表")
    @GetMapping("/list")
    public DataResponse<?> getVideoList(VideoVO vo,
                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        Date startTime = vo.getStartTime();
        Date endTime = vo.getEndTime();
        Date date = new Date();
        if (startTime.getTime() > date.getTime()) {
            return DataResponse.fail(500, "开始时间不能大于当前时间", null);
        }

        if (endTime.getTime() > date.getTime()) {
            return DataResponse.fail(500, "结束时间不能大于当前时间", null);
        }

        THardware hardware = hardwareService.getById(vo.getHardwareId());
        TSite site = siteService.getById(hardware.getSiteId());

        List<LocalDateTime[]> localDateTimes = TimeSplitter.splitByHour(startTime, endTime);
        List<VideoDataVo> videoDataVoList = new ArrayList<>();
        for (LocalDateTime[] localDateTime : localDateTimes) {
            VideoDataVo dataVo = new VideoDataVo();
            dataVo.setSiteId(site.getId());
            dataVo.setSiteName(site.getSiteName());
            dataVo.setMonitorLocation(hardware.getMonitorLocation());
            dataVo.setMonitorInfo("1小时视频");
            Instant instant = localDateTime[0].atZone(ZoneId.systemDefault()).toInstant();
            dataVo.setTime(Date.from(instant));
            videoDataVoList.add(dataVo);
        }

        List<VideoDataVo> page = ListUtil.page(pageNo - 1, pageSize, videoDataVoList);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", videoDataVoList.size());
        resultMap.put("records", page);
        return DataResponse.success(resultMap);
    }

    @ApiOperation("获取流媒体服务器地址")
    @GetMapping("/getWebRTCServer")
    public DataResponse<String> getWebRTCServer(Integer hardwareId) {
        TSysConfig webRTCServer = sysConfigService.getOne(new LambdaQueryWrapper<TSysConfig>().eq(TSysConfig::getParamKey, "webRTCServer"));
        return DataResponse.success(webRTCServer.getParamValue());
    }

    @ApiOperation("获取视频流")
    @GetMapping("/getVideoStream")
    public DataResponse<String> getVideoStream(Integer hardwareId) throws Exception {
        TCameraInfo cameraInfo = cameraInfoService.getCameraInfoByHardwareId(hardwareId);
        if (cameraInfo == null) {
            return DataResponse.fail(ErrorCode.E3022.getCode(), ErrorCode.E3022.getMessage(), null);
        }
        String url = videoStreamTemplate.replace("{username}", cameraInfo.getUsername())
                .replace("{password}", cameraInfo.getPassword())
                .replace("{ip}", cameraInfo.getIp())
                .replace("{port}", cameraInfo.getPort())
                .replace("{channelId}", String.valueOf(cameraInfo.getChannelId() * 100 + 1));
        return DataResponse.success(RTSPEncryptor.encrypt(url));
    }

    @ApiOperation("获取视频流(组织)")
    @GetMapping("/getVideoStreamByType")
    public DataResponse<List<Map<String, Object>>> getVideoStreamByType(Integer orgId, Integer monitorType) {
        List<TCameraInfo> cameraInfoList = cameraInfoService.getTCameraInfoByOrgIdAndMonitorType(orgId, monitorType);
        List<Map<String, Object>> urlList = cameraInfoList.stream().map(cameraInfo -> {
            Map<String, Object> map = new HashMap<>();
            try {
                String replace = videoStreamTemplate.replace("{username}", cameraInfo.getUsername())
                        .replace("{password}", cameraInfo.getPassword())
                        .replace("{ip}", cameraInfo.getIp())
                        .replace("{port}", cameraInfo.getPort())
                        .replace("{channelId}", String.valueOf(cameraInfo.getChannelId() * 100 + 1));

                map.put("url", RTSPEncryptor.encrypt(replace));
                map.put("siteName", cameraInfo.getSiteName());
                map.put("siteId", cameraInfo.getSiteId());
                map.put("hardwareId", cameraInfo.getHardwareId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return map;
        }).collect(Collectors.toList());
        return DataResponse.success(urlList);
    }

    @ApiOperation("获取历史视频流")
    @GetMapping("/getVideoStreamHis")
    public DataResponse<String> getVideoStreamHis(VideoVO vo) throws Exception {
        TCameraInfo cameraInfo = cameraInfoService.getCameraInfoByHardwareId(vo.getHardwareId());
        Integer nvrId = cameraInfo.getNvrId();
        TNvrInfo nrvInfo = nvrInfoService.getById(nvrId);
        String url = videoStreamHisTemplate.replace("{username}", nrvInfo.getUsername())
                .replace("{password}", nrvInfo.getPassword())
                .replace("{ip}", nrvInfo.getNvrIp())
                .replace("{port}", nrvInfo.getNvrPort())
                .replace("{channelId}", String.valueOf(cameraInfo.getNvrChannelId() * 100 + 1));

        String param = "?startTime=" + sdf.format(vo.getStartTime()) + "&endTime=" + sdf.format(vo.getEndTime());
        return DataResponse.success(RTSPEncryptor.encrypt(url + param));
    }
}
