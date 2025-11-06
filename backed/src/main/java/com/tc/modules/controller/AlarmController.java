package com.tc.modules.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tc.common.annotation.Log;
import com.tc.common.core.controller.BaseController;
import com.tc.common.enums.AlarmType;
import com.tc.common.enums.BusinessType;
import com.tc.common.http.DataResponse;
import com.tc.modules.entity.*;
import com.tc.modules.query.AlarmQuery;
import com.tc.modules.query.CommonQuery;
import com.tc.modules.query.TemperatureExportQuery;
import com.tc.modules.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * @author sunchao
 * @date 2024/3/5 15:39
 */
@Api(tags = "监控告警")
@Slf4j
@RestController
@RequestMapping("/alarm")
public class AlarmController {

    @Autowired
    private TAlarmService alarmService;

    @Autowired
    private TSiteService siteService;

    @Autowired
    private TSpeedDataService speedDataService;

    @Autowired
    private TTemperatureDataService temperatureDataService;

    @Autowired
    private TCameraInfoService cameraInfoService;

    @Autowired
    private TSpeedFFTActiveService speedFFTActiveService;

    @ApiOperation("获取告警列表")
    @GetMapping("/list")
    public DataResponse<IPage<TAlarm>> getAlarmList(AlarmQuery query,
                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        Integer orgId = query.getOrgId();
        List<Integer> siteIds = null;
        if (orgId != null) {
            LambdaQueryWrapper<TSite> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TSite::getOrgId, orgId);
            List<TSite> siteList = siteService.list(queryWrapper);
            siteIds = siteList.stream().map(TSite::getId).collect(Collectors.toList());
        }else if (query.getSiteId() != null){
            siteIds = Collections.singletonList(query.getSiteId());
        }

        Page<TAlarm> alarmPage = alarmService.getAlarmPage(new Page<>(pageNo, pageSize), query, siteIds);
        return DataResponse.success(alarmPage);
    }

    @PostMapping("/check")
    @ApiOperation("告警处理")
    @Log(title = "查看告警", businessType = BusinessType.UPDATE)
    public DataResponse<?> checkAlarm(@RequestBody TAlarm alarm, HttpServletRequest request) {
        String userId = request.getHeader("userId");
        LambdaUpdateWrapper<TAlarm> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(TAlarm::getIsCheck, 1);
        updateWrapper.set(TAlarm::getCheckUser, userId);
        updateWrapper.eq(TAlarm::getId, alarm.getId());
        alarmService.update(updateWrapper);
        return DataResponse.success();
    }

    @GetMapping("/getSpeedData")
    @ApiOperation("获取速度告警数据")
    public DataResponse<?> getSpeedData(CommonQuery query) {

        List<TSpeedData> list = speedDataService.list(new LambdaQueryWrapper<TSpeedData>()
                .eq(TSpeedData::getSiteId, query.getSiteId())
                .gt(TSpeedData::getCreateTime, query.getStartTime())
                .lt(TSpeedData::getCreateTime, query.getEndTime())
        );
        List<String[]> res = new ArrayList<>();
        List<JSONObject> resObject = new ArrayList<>();
        for (TSpeedData tSpeedData : list) {
            Double speed = tSpeedData.getSpeed();
            String time = DateUtil.format(tSpeedData.getCreateTime(), "yyyy-MM-dd HH:mm:ss.SSS");
            String[] arr = new String[]{time, speed + ""};
            res.add(arr);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "speed");
        jsonObject.put("siteId", query.getSiteId());
        jsonObject.put("value", res);
        resObject.add(jsonObject);
        return DataResponse.success(resObject);
    }

    @GetMapping("/getFluctuateData")
    @ApiOperation("获取稳定性告警数据")
    public DataResponse<?> getFluctuateData(CommonQuery query) {

        List<TSpeedData> list = speedDataService.list(new LambdaQueryWrapper<TSpeedData>()
                .eq(TSpeedData::getSiteId, query.getSiteId())
                .gt(TSpeedData::getCreateTime, query.getStartTime())
                .lt(TSpeedData::getCreateTime, query.getEndTime())
        );
        List<String[]> fluctuateDelayArr = new ArrayList<>();
        List<String[]> speedDelayArr = new ArrayList<>();

        List<List<Object>> resObject = new ArrayList<>();

        for (TSpeedData tSpeedData : list) {
            Double speedDelay = tSpeedData.getSpeedDelay();
            Double fluctuateDelay = tSpeedData.getSpeedFluctuateDelay();
            String time = DateUtil.format(tSpeedData.getCreateTime(), "yyyy-MM-dd HH:mm:ss.SSS");
            String[] arr1 = new String[]{time, speedDelay + ""};
            String[] arr2 = new String[]{time, fluctuateDelay + ""};

            speedDelayArr.add(arr1);
            fluctuateDelayArr.add(arr2);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "speedDelay");
        jsonObject.put("siteId", query.getSiteId());
        jsonObject.put("value", speedDelayArr);

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("name", "fluctuateDelay");
        jsonObject2.put("siteId", query.getSiteId());
        jsonObject2.put("value", fluctuateDelayArr);

        resObject.add(Arrays.asList(jsonObject, jsonObject2));
        return DataResponse.success(resObject);
    }

    @GetMapping("/getAverageData")
    @ApiOperation("获取震荡告警数据")
    public DataResponse<?> getAverageData(CommonQuery query) {

        List<TSpeedData> list = speedDataService.list(new LambdaQueryWrapper<TSpeedData>()
                .eq(TSpeedData::getSiteId, query.getSiteId())
                .gt(TSpeedData::getCreateTime, query.getStartTime())
                .lt(TSpeedData::getCreateTime, query.getEndTime())
        );

        List<String[]> averageDelayArr = new ArrayList<>();
        List<String[]> speedDelayArr = new ArrayList<>();

        List<List<Object>> resObject = new ArrayList<>();

        for (TSpeedData tSpeedData : list) {
            Double speedDelay = tSpeedData.getSpeedDelay();
            Double averageDelay = tSpeedData.getSpeedAverageDelay();
            String time = DateUtil.format(tSpeedData.getCreateTime(), "yyyy-MM-dd HH:mm:ss.SSS");
            String[] arr1 = new String[]{time, speedDelay + ""};
            String[] arr2 = new String[]{time, averageDelay + ""};

            speedDelayArr.add(arr1);
            averageDelayArr.add(arr2);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "speedDelay");
        jsonObject.put("siteId", query.getSiteId());
        jsonObject.put("value", speedDelayArr);

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("name", "averageDelay");
        jsonObject2.put("siteId", query.getSiteId());
        jsonObject2.put("value", averageDelayArr);

        resObject.add(Arrays.asList(jsonObject, jsonObject2));
        return DataResponse.success(resObject);
    }

    @GetMapping("/getTemperatureData")
    @ApiOperation("获取温度告警数据")
    public DataResponse<?> getTemperatureData(CommonQuery query) {

        TCameraInfo cameraInfo = cameraInfoService.getCameraInfoByHardwareId(query.getHardwareId());
        List<TTemperatureData> temperatureDataList = temperatureDataService.list(new LambdaQueryWrapper<TTemperatureData>()
                .eq(TTemperatureData::getCameraId, cameraInfo.getId())
                .gt(TTemperatureData::getCreateTime, query.getStartTime())
                .lt(TTemperatureData::getCreateTime, query.getEndTime())
        );
        Map<Integer, List<TTemperatureData>> ruleIdMap = temperatureDataList.stream().collect(Collectors.groupingBy(TTemperatureData::getRuleId));

        List<JSONObject> resObject = new ArrayList<>();

        for (List<TTemperatureData> values : ruleIdMap.values()) {
            List<String[]> res = new ArrayList<>();
            for (TTemperatureData value : values) {
                String temperature = value.getCurrentTemperature() == null ? value.getMaxTemperature() : value.getCurrentTemperature();
                double doubleValue = new BigDecimal(temperature).setScale(2, RoundingMode.HALF_UP).doubleValue();
                String time = DateUtil.format(value.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
                String[] arr = new String[]{time, doubleValue+""};
                res.add(arr);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ruleId", values.get(0).getRuleId());
            jsonObject.put("name", values.get(0).getRuleName());
            jsonObject.put("value", res);
            resObject.add(jsonObject);
        }
        return DataResponse.success(resObject);
    }

    @GetMapping("/speedingExport")
    @ApiOperation(value = "超速导出(告警数据)")
    @Log(title = "超速导出(告警数据)", businessType = BusinessType.EXPORT)
    public ResponseEntity<byte[]> speedingExport(@RequestParam(value = "siteId",required = true) Integer siteId,
                                                 @RequestParam(value = "alarmId",required = false) Integer alarmId) throws IOException {
        return speedDataService.exportSpeedData(siteId, null,1,alarmId);
    }


    @GetMapping("/stabilityExport")
    @ApiOperation(value = "转速稳定性导出(告警数据)")
    @Log(title = "转速稳定性导出(告警数据)", businessType = BusinessType.EXPORT)
    public ResponseEntity<byte[]> stabilityExport(@RequestParam(value = "siteId",required = true) Integer siteId,
                                                  @RequestParam(value = "alarmId",required = false) Integer alarmId) throws IOException {
        return speedDataService.exportSpeedData(siteId, null,2,alarmId);
    }

    @GetMapping("/AnalysisExport")
    @ApiOperation(value = "转速频谱导出(告警数据)")
    @Log(title = "转速频谱导出(告警数据)", businessType = BusinessType.EXPORT)
    public ResponseEntity<byte[]> speedAnalysisExport(@RequestParam(value = "alarmId",required = true) Integer alarmId) throws IOException {
        return speedFFTActiveService.AnalysisExport(null,alarmId);
    }

    @GetMapping("/temperatureExport")
    @ApiOperation(value = "温度导出(告警数据)")
    public ResponseEntity<byte[]> temperatureExport(TemperatureExportQuery query) throws IOException {
        return temperatureDataService.temperatureExport(query.getEndTime(), query.getAlarmId(), query.getSiteId(), query.getHardwareId());
    }
}
