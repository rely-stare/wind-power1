package com.tc.modules.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tc.common.annotation.Log;
import com.tc.common.enums.BusinessType;
import com.tc.common.http.DataResponse;
import com.tc.modules.api.HkCameraApi;
import com.tc.modules.entity.TSpeedData;
import com.tc.modules.entity.TSpeedFFTActive;
import com.tc.modules.entity.TTemperatureData;
import com.tc.modules.export.*;
import com.tc.modules.query.CommonQuery;
import com.tc.modules.service.TSpeedDataService;
import com.tc.modules.service.TSpeedFFTActiveService;
import com.tc.modules.service.TTemperatureDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "数据中心")
@RestController
@RequestMapping("/dataCenter")
public class DataCenterController {

    @Autowired
    private TSpeedDataService speedDataService;

    @Autowired
    private TSpeedFFTActiveService speedFFTActiveService;

    @Autowired
    private TTemperatureDataService temperatureDataService;

    @Autowired
    private HkCameraApi hkCameraApi;

    @Value("${file.video.folder}")
    private String videoFolder;

    @GetMapping("/speedExport")
    @Log(title = "数据中心-转速导出", businessType = BusinessType.EXPORT)
    @ApiOperation("数据中心-转速导出")
    public void speedExport(@RequestParam Integer siteId, @RequestParam String startTime,
                            @RequestParam String endTime, HttpServletResponse response) throws IOException {

        DateTime start = DateUtil.parse(startTime);
        DateTime end = DateUtil.parse(endTime);

        // 时间范围校验
        long between = DateUtil.between(start, end, DateUnit.DAY);
        long between1 = DateUtil.between(start, new Date(), DateUnit.DAY);

        if (between > 1) {
            writeErrorResponse(response, 500, "导出失败，时间范围不能超过一天");
        }
        if (between1 > 30) {
            writeErrorResponse(response, 500, "导出失败，导出时间不能超过30天");
        }

        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("speed_data_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 获取数据并导出
            List<TSpeedData> speedDataByInfluxDB = speedDataService.getSpeedDataByInfluxDB(siteId, start, end);
            List<SpeedDataExport> reportData = speedDataByInfluxDB.stream().map(s -> {
                SpeedDataExport speedData = new SpeedDataExport();
                BeanUtils.copyProperties(s, speedData);
                return speedData;
            }).collect(Collectors.toList());

            EasyExcel.write(response.getOutputStream(), SpeedDataExport.class)
                    .autoCloseStream(Boolean.FALSE)
                    .sheet("speed_data")
                    .doWrite(reportData);
        } catch (Exception e) {
            writeErrorResponse(response, 500, "导出失败，系统内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/speedStabilityExport")
    @Log(title = "数据中心-稳定性导出", businessType = BusinessType.EXPORT)
    @ApiOperation("数据中心-稳定性导出")
    public void stabilityExport(@RequestParam Integer siteId, @RequestParam String startTime,
                                      @RequestParam String endTime, HttpServletResponse response) throws IOException {

        DateTime start = DateUtil.parse(startTime);
        DateTime end = DateUtil.parse(endTime);

        // 时间范围校验
        long between = DateUtil.between(start, end, DateUnit.DAY);
        long between1 = DateUtil.between(start, new Date(), DateUnit.DAY);

        if (between > 1) {
            writeErrorResponse(response, 500, "导出失败，时间范围不能超过一天");
        }
        if (between1 > 30) {
            writeErrorResponse(response, 500, "导出失败，导出时间不能超过30天");
        }

        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("speed_stability_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 获取数据并导出
            List<TSpeedData> speedDataByInfluxDB = speedDataService.getSpeedDataByInfluxDB(siteId, start, end);
            List<SpeedStabilityExport> reportStabilityData = speedDataByInfluxDB.stream().map(s -> {
                SpeedStabilityExport stability = new SpeedStabilityExport();
                BeanUtils.copyProperties(s, stability);
                return stability;
            }).collect(Collectors.toList());

            EasyExcel.write(response.getOutputStream(), SpeedStabilityExport.class)
                    .autoCloseStream(Boolean.FALSE)
                    .sheet("speed_stability_data")
                    .doWrite(reportStabilityData);
        } catch (Exception e) {
            writeErrorResponse(response, 500, "导出失败，系统内部错误：" + e.getMessage());
        }

    }

    @GetMapping("/speedFluctuationExport")
    @Log(title = "数据中心-震荡导出", businessType = BusinessType.EXPORT)
    @ApiOperation("数据中心-震荡导出")
    public void FluctuationExport(@RequestParam Integer siteId, @RequestParam String startTime,
                                @RequestParam String endTime, HttpServletResponse response) throws IOException {

        DateTime start = DateUtil.parse(startTime);
        DateTime end = DateUtil.parse(endTime);

        // 时间范围校验
        long between = DateUtil.between(start, end, DateUnit.DAY);
        long between1 = DateUtil.between(start, new Date(), DateUnit.DAY);

        if (between > 1) {
            writeErrorResponse(response, 500, "导出失败，时间范围不能超过一天");
        }
        if (between1 > 30) {
            writeErrorResponse(response, 500, "导出失败，导出时间不能超过30天");
        }

        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("speed_fluctuation_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 获取数据并导出
            List<TSpeedData> speedDataByInfluxDB = speedDataService.getSpeedDataByInfluxDB(siteId, start, end);
            List<SpeedFluctuationExport> fluctuationData = speedDataByInfluxDB.stream().map(s -> {
                SpeedFluctuationExport fluctuationExport = new SpeedFluctuationExport();
                BeanUtils.copyProperties(s, fluctuationExport);
                return fluctuationExport;
            }).collect(Collectors.toList());

            EasyExcel.write(response.getOutputStream(), SpeedFluctuationExport.class)
                    .autoCloseStream(Boolean.FALSE)
                    .sheet("speed_fluctuation_data")
                    .doWrite(fluctuationData);
        } catch (Exception e) {
            writeErrorResponse(response, 500, "导出失败，系统内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/speedSpectrumExport")
    @Log(title = "数据中心-频谱导出", businessType = BusinessType.EXPORT)
    @ApiOperation("数据中心-频谱导出")
    public void pectrumExport(@RequestParam Integer id,HttpServletResponse response) throws IOException {

        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("speed_spectrum_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 获取数据并导出
            TSpeedFFTActive speedFFTActive = speedFFTActiveService.getById(id);
            JSONObject jsonObject = JSON.parseObject(speedFFTActive.getResult());

            JSONObject spectrum = jsonObject.getJSONObject("spectrum");

            JSONArray xArray = spectrum.getJSONArray("x");
            JSONArray yArray = spectrum.getJSONArray("y");

            List<SpeedSpectrumExport> spectrumExportList = new ArrayList<>();
            for (int i = 0; i < xArray.size(); i++) {
                SpeedSpectrumExport speedSpectrumExport = new SpeedSpectrumExport(xArray.getString(i), yArray.getString(i));
                spectrumExportList.add(speedSpectrumExport);
            }

            EasyExcel.write(response.getOutputStream(), SpeedSpectrumExport.class)
                    .autoCloseStream(Boolean.FALSE)
                    .sheet("speed_spectrum_data")
                    .doWrite(spectrumExportList);
        } catch (Exception e) {
            writeErrorResponse(response, 500, "导出失败，系统内部错误：" + e.getMessage());
        }
    }


    @GetMapping("/temperatureExport")
    @Log(title = "数据中心-最高温导出", businessType = BusinessType.EXPORT)
    @ApiOperation("数据中心-最高温导出")
    public void temperatureExport(@RequestParam Integer hardwareId, @RequestParam String startTime,
                                  @RequestParam String endTime, HttpServletResponse response) throws IOException {

        DateTime start = DateUtil.parse(startTime);
        DateTime end = DateUtil.parse(endTime);

        // 时间范围校验
        long between = DateUtil.between(start, end, DateUnit.DAY);
        long between1 = DateUtil.between(start, new Date(), DateUnit.DAY);

        if (between > 1) {
            writeErrorResponse(response, 500, "导出失败，时间范围不能超过一天");
        }
        if (between1 > 30) {
            writeErrorResponse(response, 500, "导出失败，导出时间不能超过30天");
        }

        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("temperature_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 获取数据并导出
            List<TTemperatureData> temperatureData = temperatureDataService.getTemperatureDataListByInfluxDB(new CommonQuery(null, hardwareId, start, end));

            List<TemperatureDataExport> temperatureDataExports = temperatureData.stream().map(s -> {
                TemperatureDataExport temperatureDataExport = new TemperatureDataExport();
                BeanUtils.copyProperties(s, temperatureDataExport);
                return temperatureDataExport;
            }).collect(Collectors.toList());


            EasyExcel.write(response.getOutputStream(), TemperatureDataExport.class)
                    .autoCloseStream(Boolean.FALSE)
                    .sheet("temperature_data")
                    .doWrite(temperatureDataExports);
        } catch (Exception e) {
            writeErrorResponse(response, 500, "导出失败，系统内部错误：" + e.getMessage());
        }
    }


    @GetMapping("/videoDownload")
    @Log(title = "数据中心-视频下载", businessType = BusinessType.EXPORT)
    @ApiOperation("数据中心-视频下载")
    public DataResponse<String> videoDownload(@RequestParam Integer hardwareId, @RequestParam String startTime,
                              @RequestParam String endTime, HttpServletResponse response) throws IOException {

        DateTime start = DateUtil.parse(startTime);
        DateTime end = DateUtil.parse(endTime);

        // 时间范围校验
        long between = DateUtil.between(start, end, DateUnit.DAY);
        long between1 = DateUtil.between(start, new Date(), DateUnit.DAY);

        if (between > 1) {
            writeErrorResponse(response, 500, "导出失败，时间范围不能超过一天");
        }
        if (between1 > 30) {
            writeErrorResponse(response, 500, "导出失败，导出时间不能超过30天");
        }

        String fileName = videoFolder + File.separator + "temp" + "video_" + System.currentTimeMillis() + ".mp4";
        hkCameraApi.downloadRecordByTime(hardwareId, DateUtil.parse(startTime), DateUtil.parse(endTime), fileName);

        String address = InetAddress.getLocalHost().getHostAddress();
        return DataResponse.success(address + "/" + fileName);
    }

    // 写入错误响应
    private void writeErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.reset();
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        DataResponse<Object> errorResponse = new DataResponse<>(statusCode, message, null);
        response.getWriter().println(JSON.toJSONString(errorResponse));
    }

}
