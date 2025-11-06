package com.tc.modules.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.tc.common.enums.MonitorType;
import com.tc.modules.entity.TAlarm;
import com.tc.modules.entity.TSpeedData;
import com.tc.modules.entity.TSpeedFFTActive;
import com.tc.modules.mapper.TAlarmMapper;
import com.tc.modules.mapper.TSpeedDataMapper;
import com.tc.modules.mapper.TSpeedFFTActiveMapper;
import com.tc.modules.service.TSpeedDataService;
import com.tc.modules.service.TSpeedFFTActiveService;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class TSpeedDataServiceImpl extends ServiceImpl<TSpeedDataMapper, TSpeedData> implements TSpeedDataService {

    @Autowired
    private InfluxDBClient client;

    @Value("${influxdb.bucket}")
    private String BUCKET;

    @Value("${python.alias}")
    private String pythonAlias;

    @Value("${python.script}")
    private String pythonScript;

    @Autowired
    private TSpeedFFTActiveService speedFFTActiveService;

    @Autowired
    private TSpeedDataMapper tSpeedDataMapper;

    @Autowired
    private TSpeedFFTActiveMapper tSpeedFFTActiveMapper;

    @Autowired
    private TAlarmMapper talarmMapper;

    @Override
    public List<TSpeedData> getSpeedDataByInfluxDB(int siteId, Date startTime, Date endTime) {

        // 创建 SimpleDateFormat 对象，用于格式化时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        // 设置时区为 UTC
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String start = sdf.format(startTime);
        String stop = sdf.format(endTime);

        String fluxQuery = "import \"influxdata/influxdb/v1\" \n" +
                "from(bucket: \"" + BUCKET + "\") " +
                "  |> range(start:" + start + ", stop:" + stop + ") " +
                "  |> filter(fn: (r) => r._measurement == \"speed_data\")\n" +
                "  |> filter(fn: (r) => r.device_id == \"" + siteId + "\")\n" +
                "  |> v1.fieldsAsCols()";

        // 执行查询
        List<FluxTable> tables = client.getQueryApi().query(fluxQuery);

        List<TSpeedData> speedDataList = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, Object> map = record.getValues();
                Instant time = (Instant) map.get("_time");
                int siteId1 = Integer.parseInt(map.get("device_id").toString());
                double rGeneratorSpeed = (double) map.get("rGeneratorSpeed");
                double rGeneratorSpeedDelay = (double) map.get("rGeneratorSpeedDelay");
                double rGeneratorSpeedFluctuateDelay = (double) map.get("rGeneratorSpeedFluctuateDelay");
                double rGeneratorSpeedAverageDelay = (double) map.get("rGeneratorSpeedAverageDelay");

                TSpeedData data = new TSpeedData();
                data.setCreateTime(Date.from(time));
                data.setSiteId(siteId1);
                data.setSpeed(rGeneratorSpeed);
                data.setSpeedDelay(rGeneratorSpeedDelay);
                data.setSpeedFluctuateDelay(rGeneratorSpeedFluctuateDelay);
                data.setSpeedAverageDelay(rGeneratorSpeedAverageDelay);
                speedDataList.add(data);
            }
        }
        return speedDataList;
    }

    @Override
    @Async
    public void frequencyAnalyse(List<TSpeedData> data, int siteId, Date activeTime) {
        String Content = Arrays.toString(data.stream().map(TSpeedData::getSpeed).toArray());
        String currentDir = System.getProperty("user.dir");
        long currentTimeMillis = System.currentTimeMillis();

        String inputPath = currentDir + File.separator + "temp" + File.separator + "input_" + currentTimeMillis + ".txt";
        String outPath = currentDir + File.separator + "temp" + File.separator + "output_" + currentTimeMillis + ".txt";

        FileUtil.writeString(Content, inputPath, "UTF-8");
        RuntimeUtil.execForStr(pythonAlias, pythonScript + File.separator + "speedAnalysisMain.py", inputPath, outPath);

        String outContent = FileUtil.readUtf8String(outPath);
        JSONObject jsonObject = JSON.parseObject(outContent);

        TSpeedFFTActive active = new TSpeedFFTActive();
        active.setSiteId(siteId);
        active.setActiveTime(activeTime);
        active.setResult(jsonObject.toJSONString());
        speedFFTActiveService.save(active);
    }

    /**
     * 导出数据
     *
     * @param siteId，endTime
     * @param endTime
     * @param monitorType
     * @param alarmId
     * @return
     * @throws IOException
     */
    @Override
    public ResponseEntity<byte[]> exportSpeedData(int siteId, Date endTime, Integer monitorType,Integer alarmId) throws IOException {
        // 获取数据
        List<TSpeedData> speedDataList = getSpeedData(siteId, endTime, monitorType,alarmId);
        log.info("获取数据：{}", speedDataList);

        // 填充数据到excel中
        ByteArrayOutputStream byteArrayOutputStream = getByteArrayOutputStream(speedDataList, monitorType);
        String encodedFileName = URLEncoder.encode("speed_data_" + UUID.fromString(String.valueOf(UUID.randomUUID())) + ".xlsx", "UTF-8");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", encodedFileName);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(byteArrayOutputStream.toByteArray());
    }

    /**
     * 获取数据
     *
     * @param siteId
     * @param endTime
     * @param monitorType
     * @return
     */
    private @NotNull List<TSpeedData> getSpeedData(int siteId, Date endTime, Integer monitorType, Integer alarmId) {
        Date startTime = null;
        if (Objects.nonNull(endTime)){
            Map<Integer, Long> typeToOffsetMap = new HashMap<>();
            typeToOffsetMap.put(MonitorType.SPEED_OVERLIMIT.getType(), -5L);
            typeToOffsetMap.put(MonitorType.SPEED_STABILITY.getType(), -1L);
            typeToOffsetMap.put(MonitorType.SPEED_OSCILLATION.getType(), -10L);
            Long offsetMinutes = typeToOffsetMap.get(monitorType);
            Assert.notNull(offsetMinutes, "无效的监控类型");
            startTime = new Date(endTime.getTime() + offsetMinutes * 60 * 1000);
        }
        if (Objects.nonNull(alarmId)){
            // 告警数据查询
            return getAlarmSpeedData(siteId, alarmId);
        }
         return getSpeedDataByInfluxDB(siteId, startTime, endTime);
    }

    /**
     * 获取告警数据
     *
     * @param siteId
     * @param alarmId
     * @return
     */
    private List<TSpeedData> getAlarmSpeedData(int siteId,  Integer alarmId) {
        // 告警数据查询
        Date time = talarmMapper.selectOne(new LambdaQueryWrapper<TAlarm>()
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

        List<TSpeedData> tSpeedDataList = tSpeedDataMapper.selectList(new LambdaQueryWrapper<TSpeedData>()
                .eq(TSpeedData::getSiteId, siteId)
                .ge(TSpeedData::getCreateTime, startTimeDate)
                .le(TSpeedData::getCreateTime, endTimeDate));
        log.info("告警数据查询：{}", tSpeedDataList);
        return tSpeedDataList;
    }

    /**
     * 获取文件流
     *
     * @param speedDataList
     * @param monitorType
     * @return
     * @throws IOException
     */
    private static @NotNull ByteArrayOutputStream getByteArrayOutputStream(List<TSpeedData> speedDataList, Integer monitorType) throws IOException {
        Map<Integer, List<String>> cellValue = new HashMap<>();
        cellValue.put(1, Arrays.asList("Time", "Site ID", "GeneratorSpeed"));
        cellValue.put(2, Arrays.asList("Time", "Site ID", "GeneratorSpeedDelay", "GeneratorSpeedFluctuateDelay"));
        cellValue.put(3, Arrays.asList("Time", "Site ID", "GeneratorSpeedDelay", "GeneratorSpeedAverageDelay"));
        return getByteArrayOutputStream(speedDataList, monitorType, cellValue);
    }

    /**
     * 填充数据
     *
     * @param speedDataList
     * @param monitorType
     * @param cellValue
     * @return
     * @throws IOException
     */
    private static @NotNull ByteArrayOutputStream getByteArrayOutputStream(List<TSpeedData> speedDataList, Integer monitorType, Map<Integer, List<String>> cellValue) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Speed Data");
        // 创建表头
        Row headerRow = sheet.createRow(0);
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setAlignment(HorizontalAlignment.LEFT);
        List<String> headerList = cellValue.get(monitorType);
        Assert.notNull(headerList, "无效的监控类型");

        for (int i = 0; i < headerList.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headerList.get(i));
            cell.setCellStyle(headerCellStyle);
        }


        // 创建日期格式
        DataFormat dataFormat = workbook.createDataFormat();
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setAlignment(HorizontalAlignment.LEFT);
        dateCellStyle.setDataFormat(dataFormat.getFormat("yyyy-MM-dd HH:mm:ss.SSS"));

        // 创建默认单元格样式
        CellStyle defaultCellStyle = workbook.createCellStyle();
        defaultCellStyle.setAlignment(HorizontalAlignment.LEFT);

        // 填充数据
        int rowNum = 1;
        for (TSpeedData data : speedDataList) {
            Row row = sheet.createRow(rowNum++);

            // 设置时间单元格
            Cell timeCell = row.createCell(0);
            timeCell.setCellValue(data.getCreateTime());
            timeCell.setCellStyle(dateCellStyle);

            // 设置SiteID单元格
            Cell SiteCell = row.createCell(1);
            SiteCell.setCellValue(data.getSiteId());
            SiteCell.setCellStyle(defaultCellStyle);

            // 数据单元格
            for (int i = 2; i < headerList.size(); i++) {
                String columnName = headerList.get(i);
                Cell dataCell = row.createCell(i);
                switch (columnName) {
                    case "GeneratorSpeed":
                        dataCell.setCellValue(data.getSpeed());
                        break;
                    case "GeneratorSpeedDelay":
                        dataCell.setCellValue(data.getSpeedDelay());
                        break;
                    case "GeneratorSpeedFluctuateDelay":
                        dataCell.setCellValue(data.getSpeedFluctuateDelay());
                        break;
                    case "GeneratorSpeedAverageDelay":
                        dataCell.setCellValue(data.getSpeedAverageDelay());
                        break;
                    default:
                        dataCell.setCellValue("");
                        break;
                }
                dataCell.setCellStyle(defaultCellStyle);
            }

        }

        // 自动调整列宽
        for (int i = 0; i < headerList.size(); i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();
        return byteArrayOutputStream;
    }
}
