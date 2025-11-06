package com.tc.modules.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.modules.entity.TAlarm;
import com.tc.modules.entity.TSpeedFFTActive;
import com.tc.modules.entity.TTemperatureData;
import com.tc.modules.mapper.TAlarmMapper;
import com.tc.modules.mapper.TSpeedFFTActiveMapper;
import com.tc.modules.mapper.TTemperatureDataMapper;
import com.tc.modules.service.TSpeedFFTActiveService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class TSpeedFFTActiveServiceImpl extends ServiceImpl<TSpeedFFTActiveMapper, TSpeedFFTActive> implements TSpeedFFTActiveService {

    @Autowired
    private TSpeedFFTActiveMapper tSpeedFFTActiveMapper;

    @Autowired
    private TAlarmMapper tAlarmMapper;

    @Autowired
    private TTemperatureDataMapper tTemperatureDataMapper;

    @Override
    public ResponseEntity<byte[]> AnalysisExport(Integer id, Integer alarmId) throws IOException {
        String result = "";
        if (Objects.nonNull(id)) {
            result = tSpeedFFTActiveMapper.selectOne(new LambdaQueryWrapper<TSpeedFFTActive>()
                            .eq(TSpeedFFTActive::getId, id)
                            .select(TSpeedFFTActive::getResult))
                    .getResult();
        } else {
            // 告警数据查询
            result = tSpeedFFTActiveMapper.selectOne(new LambdaQueryWrapper<TSpeedFFTActive>()
                            .eq(TSpeedFFTActive::getAlarmId, alarmId)
                            .select(TSpeedFFTActive::getResult))
                    .getResult();
        }
        JSONObject jsonObject = JSON.parseObject(result);
        JSONObject spectrum = jsonObject.getJSONObject("spectrum");
        JSONArray xArray = spectrum.getJSONArray("x");
        JSONArray yArray = spectrum.getJSONArray("y");

        ByteArrayOutputStream byteArrayOutputStream = getByteArrayOutputStream(xArray, yArray);
        String encodedFileName = URLEncoder.encode("spectrum.xlsx", "UTF-8");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", encodedFileName);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(byteArrayOutputStream.toByteArray());
    }

    /**
     * 频谱响应实体
     *
     * @param xArray
     * @param yArray
     * @return
     * @throws UnsupportedEncodingException
     */
    private static @NotNull ByteArrayOutputStream getByteArrayOutputStream(JSONArray xArray, JSONArray yArray) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("spectrum");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setAlignment(HorizontalAlignment.LEFT);

        Cell xHeaderCell = headerRow.createCell(0);
        xHeaderCell.setCellValue("x");
        xHeaderCell.setCellStyle(headerCellStyle);

        Cell yHeaderCell = headerRow.createCell(1);
        yHeaderCell.setCellValue("y");
        yHeaderCell.setCellStyle(headerCellStyle);

        // 创建默认单元格样式
        CellStyle defaultCellStyle = workbook.createCellStyle();
        defaultCellStyle.setAlignment(HorizontalAlignment.LEFT);

        // 设置数字格式
        DataFormat dataFormat = workbook.createDataFormat();
        CellStyle numberCellStyle = workbook.createCellStyle();
        numberCellStyle.setAlignment(HorizontalAlignment.LEFT);
        numberCellStyle.setDataFormat(dataFormat.getFormat("0.0000000000000000"));

        // 填充数据
        for (int i = 0; i < xArray.size(); i++) {
            Row row = sheet.createRow(i + 1);

            Cell xCell = row.createCell(0);
            xCell.setCellValue(xArray.getDouble(i));
            xCell.setCellStyle(numberCellStyle);

            Cell yCell = row.createCell(1);
            yCell.setCellValue(yArray.getDouble(i));
            yCell.setCellStyle(numberCellStyle);
        }

        // 自动调整列宽
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();
        return byteArrayOutputStream;
    }
}
