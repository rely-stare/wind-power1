package com.tc.modules.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tc.common.enums.AlarmType;
import com.tc.common.http.DataResponse;
import com.tc.common.redis.RedisCache;
import com.tc.modules.entity.TAlarm;
import com.tc.modules.entity.TCameraInfo;
import com.tc.modules.entity.THardware;
import com.tc.modules.entity.TSite;
import com.tc.modules.service.*;
import com.tc.modules.vo.OverviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sunchao
 * @date 2023/12/11 13:49
 */
@Api(tags = "首页")
@RestController
@RequestMapping("/overview")
public class OverviewController {

    @Autowired
    private TSiteService siteService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TAlarmService alarmService;

    @Autowired
    private TCameraInfoService cameraInfoService;


    private static final String TEMPERATURE_STATUS_PREFIX = "iot:device-status:temperature:";
    private static final String SPEED_STATUS_PREFIX = "iot:device-status:speed:";

    private static final List<Integer> ALARM_TYPES = Arrays.asList(1, 2, 3, 4);


    /**
     * 请求站点状态信息
     *
     * @param siteId 可选参数，站点ID，用于指定查询的站点
     * @param orgId  必需参数，组织ID，用于指定查询的组织
     * @return 返回包含站点状态信息的DataResponse对象
     */
    @GetMapping("/getSiteStatus")
    @ApiOperation(value = "请求风机状态信息")
    public DataResponse<List<OverviewVO>> getSiteStatus(@RequestParam(required = false) Integer siteId, Integer orgId) {

        if (orgId == null) {
            return DataResponse.fail(400, "orgId is null", null);
        }

        LambdaQueryWrapper<TSite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(siteId != null, TSite::getId, siteId);
        queryWrapper.eq(TSite::getOrgId, orgId);
        List<TSite> siteList = siteService.list(queryWrapper);

        List<OverviewVO> voList = siteList.stream().map(site -> {
            OverviewVO overviewVO = new OverviewVO();
            overviewVO.setSiteId(site.getId());
            overviewVO.setSiteName(site.getSiteName());
            overviewVO.setSpeedStatus(getSpeedStatus(site));
            overviewVO.setTemperatureStatus(getTemperatureStatus(site));
            overviewVO.setVideoStatus(getVideoStatus(site));
            overviewVO.setAudioStatus(0);
            return overviewVO;
        }).collect(Collectors.toList());

        return DataResponse.success(voList);
    }

    public int getSpeedStatus(TSite site) {
        // 先检查 Redis 缓存
        Object cacheObject = redisCache.getCacheObject(SPEED_STATUS_PREFIX + site.getId());
        if (cacheObject == null || !cacheObject.equals("true")) {
            return 1;
        }

        // 如果缓存状态是 true，查询报警记录
        long count = alarmService.count(new LambdaQueryWrapper<TAlarm>()
                .eq(TAlarm::getSiteId, site.getId())
                .in(TAlarm::getAlarmType, ALARM_TYPES)
                .eq(TAlarm::getIsCheck, 0));

        // 如果有未处理的报警，则设置为 2
        return count > 0 ? 2 : 0;
    }

    public int getTemperatureStatus(TSite site) {
        List<TCameraInfo> hardwareCameraInfo = cameraInfoService.getHardwareCameraInfo(site.getId(), 2);
        if(hardwareCameraInfo.isEmpty()){
            return 1;
        }

        for (TCameraInfo cameraInfo : hardwareCameraInfo) {
            // 先检查 Redis 缓存
            Object cacheObject = redisCache.getCacheObject(TEMPERATURE_STATUS_PREFIX + cameraInfo.getId());
            if (cacheObject == null || !cacheObject.equals("true")) {
                return 1;
            }
        }
        // 如果缓存状态是 true，查询报警记录
        long count = alarmService.count(new LambdaQueryWrapper<TAlarm>()
                .eq(TAlarm::getSiteId, site.getId())
                .in(TAlarm::getAlarmType, AlarmType.TI_ALARM.getCode())
                .eq(TAlarm::getIsCheck, 0));

        // 如果有未处理的报警，则设置为 2
        return count > 0 ? 2 : 0;
    }

    public int getVideoStatus(TSite site) {
        List<TCameraInfo> hardwareCameraInfo = cameraInfoService.getHardwareCameraInfo(site.getId(), 2);
        if (hardwareCameraInfo.isEmpty()) {
            return 1;
        }
        for (TCameraInfo cameraInfo : hardwareCameraInfo) {
            // 先检查 Redis 缓存
            Object cacheObject = redisCache.getCacheObject(TEMPERATURE_STATUS_PREFIX + cameraInfo.getId());
            if (cacheObject == null || !cacheObject.equals("true")) {
                return 1;
            }
        }
        // 如果缓存状态是 true，查询报警记录
        long count = alarmService.count(new LambdaQueryWrapper<TAlarm>()
                .eq(TAlarm::getSiteId, site.getId())
                .in(TAlarm::getAlarmType, AlarmType.VIDEO_ALARM.getCode())
                .eq(TAlarm::getIsCheck, 0));

        // 如果有未处理的报警，则设置为 2
        return count > 0 ? 2 : 0;
    }
}
