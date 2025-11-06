package com.tc.modules.controller;


import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tc.common.annotation.Log;
import com.tc.common.enums.BusinessType;
import com.tc.common.http.DataResponse;
import com.tc.modules.entity.TLongTimeTemperature;
import com.tc.modules.entity.TTemperatureData;
import com.tc.modules.export.LongTimeTemperatureExport;
import com.tc.modules.query.CommonQuery;
import com.tc.modules.query.TemperatureExportQuery;
import com.tc.modules.service.TLongTimeTemperatureService;
import com.tc.modules.service.TTemperatureDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ti")
@Api(tags = "温度曲线")
public class TemperatureController {

    @Autowired
    private TTemperatureDataService temperatureDataService;

    @Autowired
    private TLongTimeTemperatureService tLongTimeTemperatureService;

    @ApiOperation("获取温度曲线")
    @GetMapping("/getTemperatureChart")
    public DataResponse<?> getTemperatureChart(CommonQuery query) {
        List<TTemperatureData> temperatureDataList = temperatureDataService.getTemperatureDataListByInfluxDB(query);

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


    @ApiOperation("获取长时温度")
    @GetMapping("/getLongTemperatureChart")
    public DataResponse<?> getLongTemperatureChart(CommonQuery query) {
        Date now = new Date();
        Date startTime = query.getStartTime();
        Date endTime = query.getEndTime();

        if (startTime == null) {
            startTime = DateUtil.offset(now, DateField.YEAR, -1);
        }
        if (endTime == null) {
            endTime = now;
        }

        LambdaQueryWrapper<TLongTimeTemperature> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TLongTimeTemperature::getHardwareId, query.getHardwareId());
        queryWrapper.between(TLongTimeTemperature::getDateTime, startTime, endTime);

        List<TLongTimeTemperature> temperatureList = tLongTimeTemperatureService.list(queryWrapper);

        Map<String, List<TLongTimeTemperature>> areaMap = temperatureList.stream().collect(Collectors.groupingBy(TLongTimeTemperature::getAreaName));

        List<JSONObject> resObject = new ArrayList<>();

        for (List<TLongTimeTemperature> values : areaMap.values()) {
            List<String[]> res = new ArrayList<>();
            for (TLongTimeTemperature value : values) {
                String temperature = value.getTemperature();
                double doubleValue = new BigDecimal(temperature).setScale(2, RoundingMode.HALF_UP).doubleValue();
                String time = DateUtil.format(value.getDateTime(), "yyyy-MM-dd HH:mm:ss");
                String[] arr = new String[]{time, doubleValue+""};
                res.add(arr);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", values.get(0).getAreaName());
            jsonObject.put("value", res);
            resObject.add(jsonObject);
        }
        return DataResponse.success(resObject);
    }

    @GetMapping("/temperatureExport")
    @ApiOperation(value = "温度导出")
    public ResponseEntity<byte[]> temperatureExport(TemperatureExportQuery query) throws IOException {
        return temperatureDataService.temperatureExport(query.getEndTime(), query.getAlarmId(), query.getSiteId(), query.getHardwareId());
    }


    @GetMapping("longTimeTemperatureExport")
    @ApiOperation(value = "长时温度导出")
    @Log(title = "智能监控-长时温度导出", businessType = BusinessType.EXPORT)
    public void longTimeTemperatureExport(Integer hardwareId, HttpServletResponse response) throws IOException {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("longtime_temperature_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            LambdaQueryWrapper<TLongTimeTemperature> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TLongTimeTemperature::getHardwareId, hardwareId);

            List<TLongTimeTemperature> temperatureList = tLongTimeTemperatureService.list(queryWrapper);
            List<LongTimeTemperatureExport> temperatureExportList = temperatureList.stream().map(s -> {
                return new LongTimeTemperatureExport(s.getDateTime(), s.getTemperature());
            }).collect(Collectors.toList());

            EasyExcel.write(response.getOutputStream(), LongTimeTemperatureExport.class)
                    .autoCloseStream(Boolean.FALSE)
                    .sheet("longtime_temperature_data")
                    .doWrite(temperatureExportList);
        } catch (Exception e) {
            writeErrorResponse(response, 500, "导出失败，系统内部错误：" + e.getMessage());
        }
    }

    private void writeErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.reset();
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        DataResponse<Object> errorResponse = new DataResponse<>(statusCode, message, null);
        response.getWriter().println(JSON.toJSONString(errorResponse));
    }
}
