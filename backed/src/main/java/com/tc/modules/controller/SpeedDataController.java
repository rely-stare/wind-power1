package com.tc.modules.controller;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tc.common.annotation.Log;
import com.tc.common.enums.BusinessType;
import com.tc.common.http.DataResponse;
import com.tc.modules.entity.*;
import com.tc.modules.query.CommonQuery;
import com.tc.modules.service.*;
import com.tc.modules.vo.DiffResultVo;
import com.tc.modules.vo.FFTActiveVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/speed")
@Api(tags = "speed管理")
public class SpeedDataController {

    @Autowired
    private TSpeedDataService speedDataService;

    @Autowired
    private TConfigRuleService configRuleService;

    @Autowired
    private TSpeedFFTActiveService speedFFTActiveService;

    @Autowired
    private TSiteService siteService;

    @Autowired
    private TSpeedInfoService speedInfoService;


    @GetMapping("/list")
    @ApiOperation(value = "查询speed数据")
    public DataResponse<?> getSpeedDataList(int siteId, String startTime, String endTime) {
        List<TSpeedData> speedDataByInfluxDB = speedDataService.getSpeedDataByInfluxDB(siteId, DateUtil.parse(startTime), DateUtil.parse(endTime));
        return DataResponse.success(speedDataByInfluxDB);
    }


    @GetMapping("/list/Chart")
    @ApiOperation(value = "查询转速数据")
    public DataResponse<?> getSpeedDataListChart(int siteId, String startTime, String endTime) {
        TSpeedInfo speedInfo = speedInfoService.getOne(new LambdaQueryWrapper<TSpeedInfo>().eq(TSpeedInfo::getSiteId, siteId));
        if (speedInfo == null) {
            return DataResponse.fail(500, "转速设备信息缺失！", null);
        }
        List<TSpeedData> speedDataByInfluxDB = speedDataService.getSpeedDataByInfluxDB(siteId, DateUtil.parse(startTime), DateUtil.parse(endTime));
        List<String[]> res = new ArrayList<>();

        List<JSONObject> resObject = new ArrayList<>();

        for (TSpeedData tSpeedData : speedDataByInfluxDB) {
            Double speed = tSpeedData.getSpeed();
            String time = DateUtil.format(tSpeedData.getCreateTime(), "yyyy-MM-dd HH:mm:ss.SSS");
            String[] arr = new String[]{time, speed + ""};
            res.add(arr);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "speed");
        jsonObject.put("siteId", siteId);
        jsonObject.put("value", res);

        resObject.add(jsonObject);
        return DataResponse.success(resObject);
    }

    @GetMapping("/fluctuate/Chart")
    @ApiOperation(value = "查询稳定性数据")
    public DataResponse<?> getFluctuateDataListChart(int siteId, String startTime, String endTime) {
        TSpeedInfo speedInfo = speedInfoService.getOne(new LambdaQueryWrapper<TSpeedInfo>().eq(TSpeedInfo::getSiteId, siteId));
        if (speedInfo == null) {
            return DataResponse.fail(500, "转速设备信息缺失！", null);
        }
        List<TSpeedData> speedDataByInfluxDB = speedDataService.getSpeedDataByInfluxDB(siteId, DateUtil.parse(startTime), DateUtil.parse(endTime));

        List<String[]> fluctuateDelayArr = new ArrayList<>();
        List<String[]> speedDelayArr = new ArrayList<>();

        List<List<Object>> resObject = new ArrayList<>();

        for (TSpeedData tSpeedData : speedDataByInfluxDB) {
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
        jsonObject.put("siteId", siteId);
        jsonObject.put("value", speedDelayArr);

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("name", "fluctuateDelay");
        jsonObject2.put("siteId", siteId);
        jsonObject2.put("value", fluctuateDelayArr);

        resObject.add(Arrays.asList(jsonObject, jsonObject2));
        return DataResponse.success(resObject);
    }

    @GetMapping("/average/Chart")
    @ApiOperation(value = "查询震荡数据")
    public DataResponse<?> getAverageDataListChart(int siteId, String startTime, String endTime) {
        TSpeedInfo speedInfo = speedInfoService.getOne(new LambdaQueryWrapper<TSpeedInfo>().eq(TSpeedInfo::getSiteId, siteId));
        if (speedInfo == null) {
            return DataResponse.fail(500, "转速设备信息缺失！", null);
        }
        List<TSpeedData> speedDataByInfluxDB = speedDataService.getSpeedDataByInfluxDB(siteId, DateUtil.parse(startTime), DateUtil.parse(endTime));

        List<String[]> averageDelayArr = new ArrayList<>();
        List<String[]> speedDelayArr = new ArrayList<>();

        List<List<Object>> resObject = new ArrayList<>();

        for (TSpeedData tSpeedData : speedDataByInfluxDB) {
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
        jsonObject.put("siteId", siteId);
        jsonObject.put("value", speedDelayArr);

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("name", "averageDelay");
        jsonObject2.put("siteId", siteId);
        jsonObject2.put("value", averageDelayArr);

        resObject.add(Arrays.asList(jsonObject, jsonObject2));
        return DataResponse.success(resObject);
    }

    @GetMapping("/FFT/Chart")
    @ApiOperation(value = "获取频谱")
    public DataResponse<?> getFFtChart(Integer siteId ,Integer alarmId) {
        TSpeedInfo speedInfo = speedInfoService.getOne(new LambdaQueryWrapper<TSpeedInfo>().eq(TSpeedInfo::getSiteId, siteId));
        if (speedInfo == null) {
            return DataResponse.fail(500, "转速设备信息缺失！", null);
        }
        LambdaQueryWrapper<TSpeedFFTActive> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(siteId != null, TSpeedFFTActive::getSiteId, siteId);
        queryWrapper.eq(alarmId != null, TSpeedFFTActive::getAlarmId, alarmId);
        queryWrapper.last("limit 1");

        TSpeedFFTActive fftActive = speedFFTActiveService.getOne(queryWrapper);
        String result = fftActive.getResult();

        JSONObject jsonObject = JSON.parseObject(result);
        JSONObject spectrum = jsonObject.getJSONObject("spectrum");

        JSONArray xArray = spectrum.getJSONArray("x");
        JSONArray yArray = spectrum.getJSONArray("y");

        List<String[]> speedDelayArr = new ArrayList<>();
        for (int i = 0; i < xArray.size(); i++) {
            String xValue = xArray.getString(i);
            String yValue = yArray.getString(i);

            String x = new BigDecimal(xValue).setScale(2, RoundingMode.HALF_UP).toString();
            String y = new BigDecimal(yValue).setScale(2, RoundingMode.HALF_UP).toString();
            String[] arr = new String[]{x, y};
            speedDelayArr.add(arr);
        }

        List<JSONObject> resObject = new ArrayList<>();

        JSONObject resultObject = new JSONObject();
        resultObject.put("name", "fft");
        resultObject.put("siteId", siteId);
        resultObject.put("value", speedDelayArr);

        resObject.add(resultObject);
        return DataResponse.success(resObject);
    }


    @GetMapping("/getLastFFTActive")
    @ApiOperation(value = "获取最新一条 FFT 触发记录")
    public DataResponse<?> getLastFFTActive(int siteId) {
        LambdaQueryWrapper<TSpeedFFTActive> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TSpeedFFTActive::getSiteId, siteId);
        queryWrapper.orderByDesc(TSpeedFFTActive::getActiveTime);
        queryWrapper.last("limit 1");

        TSpeedFFTActive fftActive = speedFFTActiveService.getOne(queryWrapper);
        return DataResponse.success(fftActive);
    }

    @GetMapping("/getFFTActive")
    @ApiOperation(value = "获取 FFT 触发记录")
    public DataResponse<Page<FFTActiveVo>> getFFTActive(CommonQuery query,
                                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<TSpeedFFTActive> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TSpeedFFTActive::getSiteId, query.getSiteId());
        queryWrapper.between(query.getStartTime() != null && query.getEndTime() != null, TSpeedFFTActive::getActiveTime, query.getStartTime(), query.getEndTime());
        queryWrapper.orderByDesc(TSpeedFFTActive::getActiveTime);
        Page<TSpeedFFTActive> page = speedFFTActiveService.page(new Page<>(pageNo, pageSize), queryWrapper);

        TSite site = siteService.getById(query.getSiteId());

        Page<FFTActiveVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(fftActive -> {
            FFTActiveVo fftActiveVo = new FFTActiveVo();
            fftActiveVo.setId(fftActive.getId());
            fftActiveVo.setSiteId(fftActive.getSiteId());
            fftActiveVo.setSiteName(site.getSiteName()); // 确保 site 变量已正确赋值
            fftActiveVo.setMonitorLocation("频谱");
            fftActiveVo.setMonitorInfo("10分钟数据"); // "1O" 可能是误写，应为 "10"
            fftActiveVo.setActiveTime(fftActive.getActiveTime());
            fftActiveVo.setTime(fftActive.getActiveTime());
            return fftActiveVo;
        }).collect(Collectors.toList()));

        return DataResponse.success(voPage);
    }

    @GetMapping("/getFTActiveById")
    @ApiOperation(value = "根据 id 获取 FFT")
    public DataResponse<?> getFTActiveById(int id) {
        TSpeedFFTActive fftActive = speedFFTActiveService.getById(id);

        String result = fftActive.getResult();
        JSONObject jsonObject = JSON.parseObject(result);
        JSONObject spectrum = jsonObject.getJSONObject("spectrum");

        JSONArray xArray = spectrum.getJSONArray("x");
        JSONArray yArray = spectrum.getJSONArray("y");
        List<String[]> speedDelayArr = new ArrayList<>();
        for (int i = 0; i < xArray.size(); i++) {
            String xValue = xArray.getString(i);
            String yValue = yArray.getString(i);

            String x = new BigDecimal(xValue).setScale(2, RoundingMode.HALF_UP).toString();
            String y = new BigDecimal(yValue).setScale(2, RoundingMode.HALF_UP).toString();
            String[] arr = new String[]{x, y};
            speedDelayArr.add(arr);
        }
        List<JSONObject> resObject = new ArrayList<>();
        JSONObject resultObject = new JSONObject();
        resultObject.put("name", "fft");
        resultObject.put("siteId", fftActive.getSiteId());
        resultObject.put("value", speedDelayArr);
        resObject.add(resultObject);
        return DataResponse.success(resObject);
    }


    @GetMapping("/limit")
    @ApiOperation(value = "查询speed阈值")
    public DataResponse<List<TConfigRule>> getSpeedThreshold() {
        List<String> fields = Arrays.asList("GeneratorSpeedLimit1Setpoint", "GeneratorSpeedLimit2Setpoint", "GeneratorSpeedLimit3Setpoint");
        LambdaQueryWrapper<TConfigRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TConfigRule::getModbusField, fields);
        List<TConfigRule> ruleList = configRuleService.list(queryWrapper);
        return DataResponse.success(ruleList);
    }

    @GetMapping("/speedAnalysis")
    @ApiOperation(value = "speed分析")
    public DataResponse<?> speedAnalysis(int siteId, String startTime, String endTime) {
        // 读取 转速数据
        List<TSpeedData> speedDataByInfluxDB = speedDataService.getSpeedDataByInfluxDB(siteId, DateUtil.parse(startTime), DateUtil.parse(endTime));
        speedDataService.frequencyAnalyse(speedDataByInfluxDB, siteId, DateUtil.parse(startTime));
        return DataResponse.success();
    }


    @GetMapping("/videoAnalysis")
    @ApiOperation(value = "视频图片对比分析")
    public DataResponse<?> videoAnalysis() {

        String currentDir = System.getProperty("user.dir");
        String baseImage = currentDir + File.separator + "file" + File.separator + "20241210104015.jpg";
        String currentImage = currentDir + File.separator + "file" + File.separator + "20241210110625.jpg";

        String jsonStr = RuntimeUtil.execForStr("python", "D:\\IdeaProjects\\esv\\backed\\script\\videoAnalysisMain.py", baseImage, currentImage);
        DiffResultVo diffResult = JSON.parseObject(jsonStr.trim(), DiffResultVo.class);
        return DataResponse.success(diffResult);
    }

    @GetMapping("/frequency")
    @ApiOperation(value = "查询frequency")
    public DataResponse<List<TConfigRule>> getFrequency() {
        List<String> fields = Arrays.asList("GeneratorSpeedLimit1Setpoint", "GeneratorSpeedLimit2Setpoint", "GeneratorSpeedLimit3Setpoint");
        LambdaQueryWrapper<TConfigRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TConfigRule::getHardwareType, fields);
        List<TConfigRule> ruleList = configRuleService.list(queryWrapper);
        return DataResponse.success(ruleList);
    }

    @GetMapping("/speedingExport")
    @ApiOperation(value = "超速导出")
    @Log(title = "超速导出", businessType = BusinessType.EXPORT)
    public ResponseEntity<byte[]> speedingExport(@RequestParam(value = "endTime",required = true) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS") Date endTime,
                                                  @RequestParam(value = "siteId",required = true) Integer siteId) throws IOException {
        return speedDataService.exportSpeedData(siteId, endTime,1,null);
    }


    @GetMapping("/stabilityExport")
    @ApiOperation(value = "转速稳定性导出")
    @Log(title = "转速稳定性导出", businessType = BusinessType.EXPORT)
    public ResponseEntity<byte[]> stabilityExport(@RequestParam(value = "endTime",required = true) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS") Date endTime,
                                                  @RequestParam(value = "siteId",required = true) Integer siteId) throws IOException {
        return speedDataService.exportSpeedData(siteId, endTime,2,null);
    }

    @GetMapping("/oscillatesExport")
    @ApiOperation(value = "转速震荡导出")
    @Log(title = "转速震荡导出", businessType = BusinessType.EXPORT)
    public ResponseEntity<byte[]> oscillatesExport(@RequestParam(value = "endTime",required = true) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS") Date endTime,
                                                  @RequestParam(value = "siteId",required = true) Integer siteId) throws IOException {
        return speedDataService.exportSpeedData(siteId, endTime,3,null);
    }

    @GetMapping("/AnalysisExport")
    @ApiOperation(value = "转速频谱导出")
    @Log(title = "转速频谱导出", businessType = BusinessType.EXPORT)
    public ResponseEntity<byte[]> speedAnalysisExport(@RequestParam(value = "id",required = true) Integer id,
                                                      @RequestParam(value = "alarmId",required = false) Integer alarmId) throws IOException {
        return speedFFTActiveService.AnalysisExport(id, alarmId);
    }
}
