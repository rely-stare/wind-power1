package com.tc.modules.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.tc.modules.entity.TAlarm;
import com.tc.modules.entity.TCameraInfo;
import com.tc.modules.entity.TTemperatureData;
import com.tc.modules.mapper.TAlarmMapper;
import com.tc.modules.mapper.TTemperatureDataMapper;
import com.tc.modules.query.CommonQuery;
import com.tc.modules.service.TCameraInfoService;
import com.tc.modules.service.TTemperatureDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class TTemperatureDataServiceImpl extends ServiceImpl<TTemperatureDataMapper, TTemperatureData> implements TTemperatureDataService {

    @Autowired
    private InfluxDBClient client;

    @Value("${influxdb.bucket}")
    private String BUCKET;

    @Autowired
    private TCameraInfoService cameraInfoService;

    @Autowired
    private TTemperatureDataMapper temperatureDataMapper;

    @Autowired
    private TTemperatureDataMapper tTemperatureDataMapper;

    @Autowired
    private TAlarmMapper tAlarmMapper;

    @Override
    public List<TTemperatureData> getTemperatureDataListByInfluxDB(CommonQuery query) {

        // 创建 SimpleDateFormat 对象，用于格式化时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        // 设置时区为 UTC
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String start = sdf.format(query.getStartTime());
        String stop = sdf.format(query.getEndTime());

        TCameraInfo cameraInfo = cameraInfoService.getCameraInfoByHardwareId(query.getHardwareId());

        // Flux 查询语句
        String fluxQuery = "import \"influxdata/influxdb/v1\" \n" +
                "from(bucket: \"" + BUCKET + "\") " +
                "|> range(start:" + start + ", stop:" + stop + ") " +
                "|> filter(fn: (r) => r._measurement == \"temperature\")" +
                "|> filter(fn: (r) => r.camera_id == \"" + cameraInfo.getId() + "\")" +
                "|> v1.fieldsAsCols()";

        // 执行查询
        List<FluxTable> tables = client.getQueryApi().query(fluxQuery);

        List<TTemperatureData> temperatureDataList = new ArrayList<>();

        // 解析查询结果
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                TTemperatureData data = new TTemperatureData();

                Map<String, Object> map = record.getValues();
                Instant time = (Instant) map.get("_time");
                int camera_Id = Integer.parseInt(map.get("camera_id").toString());
                int ruleId = (Integer.parseInt(map.get("rule_id").toString()));
                long channelId = (long) map.get("channel_id");
                long ruleType = (long) map.get("rule_type");
                String ruleName = (String) map.get("rule_name");
                double currentTemperature = (double) map.get("current_temperature");
                double maxTemperature = (double) map.get("max_temperature");
                double minTemperature = (double) map.get("min_temperature");
                double aveTemperature = (double) map.get("ave_temperature");

                data.setCreateTime(Date.from(time));
                data.setCameraId(camera_Id);
                data.setChannelId((int) channelId);
                data.setRuleId(ruleId);
                data.setRuleType((int) ruleType);
                data.setRuleName(ruleName);
                data.setCurrentTemperature(currentTemperature == 0 ? null : currentTemperature + "");
                data.setMaxTemperature(maxTemperature + "");
                data.setMinTemperature(minTemperature + "");
                data.setAveTemperature(aveTemperature + "");

                temperatureDataList.add(data);
            }
        }
        return temperatureDataList;
    }

    @Override
    public ResponseEntity<byte[]> temperatureExport(Date endTime, Integer alarmId, Integer siteId, Integer hardwareId) throws IOException {
        Long offsetMinutes = -10L;
        List<TTemperatureData> tTemperatureDataList = new ArrayList<>();
        if (Objects.nonNull(alarmId)) {
            // 告警数据查询
            tTemperatureDataList = getTemperatureData(alarmId);
        } else {
            Date startTime = new Date(endTime.getTime() + offsetMinutes * 60 * 1000);
            tTemperatureDataList = getTemperatureDataListByInfluxDB(new CommonQuery(siteId, hardwareId, startTime, endTime));
        }
        // 填充数据到excel中
        ByteArrayOutputStream byteArrayOutputStream = getExportTemperatureData(tTemperatureDataList);
        String encodedFileName = URLEncoder.encode("temperatureExport.xlsx", "UTF-8");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", encodedFileName);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(byteArrayOutputStream.toByteArray());
    }

    /**
     * 告警数据查询
     *
     * @param alarmId
     * @return
     */
    private List<TTemperatureData> getTemperatureData(Integer alarmId) {
        List<TTemperatureData> tTemperatureDataList;
        Date time = tAlarmMapper.selectOne(new LambdaQueryWrapper<TAlarm>()
                .eq(TAlarm::getId, alarmId)
                .select(TAlarm::getAlarmTime)).getAlarmTime();

        // 将 Date 转换为 LocalDateTime
        LocalDateTime localDateTime = time.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // 前推 10 分钟作为 startTime
        LocalDateTime startTimeForAlarm = localDateTime.minusMinutes(10);

        // 后推 10 分钟作为 endTime
        LocalDateTime endTimeForAlarm = localDateTime.plusMinutes(10);

        Date startTimeDate = Date.from(startTimeForAlarm.atZone(ZoneId.systemDefault()).toInstant());
        Date endTimeDate = Date.from(endTimeForAlarm.atZone(ZoneId.systemDefault()).toInstant());

        tTemperatureDataList = tTemperatureDataMapper.selectList(new LambdaQueryWrapper<TTemperatureData>()
                .ge(TTemperatureData::getCreateTime, startTimeDate)
                .le(TTemperatureData::getCreateTime, endTimeDate)
                .select(TTemperatureData::getMaxTemperature,
                        TTemperatureData::getMinTemperature,
                        TTemperatureData::getAveTemperature,
                        TTemperatureData::getCurrentTemperature,
                        TTemperatureData::getCreateTime,
                        TTemperatureData::getRuleType));
        return tTemperatureDataList;
    }

    private @NotNull ByteArrayOutputStream getExportTemperatureData(List<TTemperatureData> tTemperatureDataList) throws IOException {
        log.info("导出温度数据为excel:{}", tTemperatureDataList);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("temperatureData");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        CellStyle headerCellStyle = createHeaderCellStyle(workbook);
        String[] headers = {"Time", "RuleType", "CurrentTemperature", "MaxTemperature", "MinTemperature", "AveTemperature"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // 创建日期格式
        CellStyle dateCellStyle = createDateCellStyle(workbook);

        // 创建默认单元格样式
        CellStyle defaultCellStyle = createDefaultCellStyle(workbook);

        // 填充数据
        int rowNum = 1;
        for (TTemperatureData data : tTemperatureDataList) {
            Row row = sheet.createRow(rowNum++);

            // 设置时间单元格
            Cell timeCellData = row.createCell(0);
            timeCellData.setCellValue(data.getCreateTime());
            timeCellData.setCellStyle(dateCellStyle);

            // 设置 RuleType 单元格
            Cell ruleTypeCellData = row.createCell(1);
            ruleTypeCellData.setCellValue(data.getRuleType());
            ruleTypeCellData.setCellStyle(defaultCellStyle);

            // 设置 CurrentTemperature 单元格
            Cell currentTemperatureCellData = row.createCell(2);
            currentTemperatureCellData.setCellValue(data.getCurrentTemperature() != null ? Double.parseDouble(data.getCurrentTemperature()) : 0.0);
            currentTemperatureCellData.setCellStyle(defaultCellStyle);

            // 设置 MaxTemperature 单元格
            Cell maxTemperatureCellData = row.createCell(3);
            maxTemperatureCellData.setCellValue(Double.parseDouble(data.getMaxTemperature()));
            maxTemperatureCellData.setCellStyle(defaultCellStyle);

            // 设置 MinTemperature 单元格
            Cell minTemperatureCellData = row.createCell(4);
            minTemperatureCellData.setCellValue(Double.parseDouble(data.getMinTemperature()));
            minTemperatureCellData.setCellStyle(defaultCellStyle);

            // 设置 AveTemperature 单元格
            Cell aveTemperatureCellData = row.createCell(5);
            aveTemperatureCellData.setCellValue(Double.parseDouble(data.getAveTemperature()));
            aveTemperatureCellData.setCellStyle(defaultCellStyle);
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();
        return byteArrayOutputStream;
    }

    /**
     * 创建表头样式
     *
     * @param workbook
     * @return
     */
    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setAlignment(HorizontalAlignment.LEFT);
        Font font = workbook.createFont();
        font.setBold(true);
        headerCellStyle.setFont(font);
        return headerCellStyle;
    }

    /**
     * 创建日期格式样式
     *
     * @param workbook
     * @return
     */
    private CellStyle createDateCellStyle(Workbook workbook) {
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setAlignment(HorizontalAlignment.LEFT);
        DataFormat dataFormat = workbook.createDataFormat();
        dateCellStyle.setDataFormat(dataFormat.getFormat("yyyy-MM-dd HH:mm:ss.SSS"));
        return dateCellStyle;
    }

    /**
     * 创建默认样式
     *
     * @param workbook
     * @return
     */
    private CellStyle createDefaultCellStyle(Workbook workbook) {
        CellStyle defaultCellStyle = workbook.createCellStyle();
        defaultCellStyle.setAlignment(HorizontalAlignment.LEFT);
        return defaultCellStyle;
    }


}
