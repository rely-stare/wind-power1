package com.tc.modules.message.rabbitmq;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.rabbitmq.client.Channel;
import com.tc.common.enums.AlarmType;
import com.tc.common.enums.HardwareType;
import com.tc.common.enums.MonitorType;
import com.tc.modules.api.HkCameraApi;
import com.tc.modules.api.ModbusApi;
import com.tc.modules.entity.*;
import com.tc.modules.message.sse.service.SseService;
import com.tc.modules.po.TemperaturePo;
import com.tc.modules.po.TiLongTimeDataPo;
import com.tc.modules.query.CommonQuery;
import com.tc.modules.service.*;
import com.tc.modules.vo.TiInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.listener.enabled", havingValue = "true", matchIfMissing = false)
public class TemperatureMessageReceiver {

    @Autowired
    private TTemperatureDataService temperatureDataService;

    @Autowired
    private SseService sseService;

    @Autowired
    private InfluxDBClient client;

    @Autowired
    @Qualifier("threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private TCameraInfoService tCameraInfoService;

    @Autowired
    private TLongTimeTemperatureService tLongTimeTemperatureService;

    @Autowired
    private TConfigRuleService configRuleService;

    @Autowired
    private TModbusProtocolService modbusProtocolService;

    @Autowired
    private TAlarmService alarmService;

    @Autowired
    private ModbusApi modbusApi;

    @Autowired
    private HkCameraApi hkCameraApi;

    @Value("${file.video.folder}")
    private String videoFolder;

    private final Map<String, Map<Long, List<TemperaturePo>>> messageBuffer = new ConcurrentHashMap<>();

    private static final Map<String, Boolean> alarmFlag = new HashMap<>();

    private static final Map<Integer,Boolean> saveFlag = new HashMap<>();

    private final Map<Integer, Long> alarmTimeStampMap = new HashMap<>();

    private final Map<String, Long> lastRecordedTimeMap = new ConcurrentHashMap<>();



    @RabbitListener(queues = "temperature_data_queue", ackMode = "MANUAL")
    public void onMessage1(String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            TemperaturePo temperaturePo = JSON.parseObject(msg, TemperaturePo.class);
            processMessage(temperaturePo);
            // 手动确认消息
            channel.basicAck(tag, false);
        } catch (Exception e) {
            try {
                log.error("Error processing message: {}", msg, e);
                // 丢掉消息，不重新入队
                channel.basicReject(tag, false);
            } catch (Exception ex) {
                log.error("Error discarding message: {}", msg, ex);
            }
        }
    }

    @RabbitListener(queues = "ti_data_queue", ackMode = "MANUAL")
    public void onMessage2(String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            TiLongTimeDataPo temperaturePo = JSON.parseObject(msg, TiLongTimeDataPo.class);
            processMessage(temperaturePo);
            // 手动确认消息
            channel.basicAck(tag, false);
        } catch (Exception e) {
            try {
                log.error("Error processing message: {}", msg, e);
                // 丢掉消息，不重新入队
                channel.basicReject(tag, false);
            } catch (Exception ex) {
                log.error("Error discarding message: {}", msg, ex);
            }
        }
    }

    public void processMessage(TemperaturePo temperaturePo) {
        long second = temperaturePo.getCreateTime().getTime() / 1000;

        // 根据机组ID来分组存储消息
        messageBuffer
                .computeIfAbsent(temperaturePo.getHardwareId() + "", k -> new ConcurrentHashMap<>())  // 获取或创建该机组的时间戳数据
                .computeIfAbsent(second, k -> new ArrayList<>())          // 获取该秒内的消息列表
                .add(temperaturePo);

        log.debug("Received message: {}", temperaturePo);
        TTemperatureData tTemperatureData = new TTemperatureData();
        BeanUtils.copyProperties(temperaturePo, tTemperatureData);

        Point point = Point
                .measurement("temperature")
                .addTag("camera_id", temperaturePo.getCameraId() + "")  // 确保是字符串
                .addTag("rule_id", temperaturePo.getRuleId() + "")     // 确保是字符串
                .addField("channel_id", temperaturePo.getChannelId())  // 保证数值类型一致
                .addField("rule_type", temperaturePo.getRuleType())
                .addField("rule_name", temperaturePo.getRuleName() != null ? temperaturePo.getRuleName() : "")  // 避免 null
                // ⬇⬇ 这里改成 double，保证存储的是数值 ⬇⬇
                .addField("current_temperature", temperaturePo.getCurrentTemperature() != null ? Double.parseDouble(temperaturePo.getCurrentTemperature()) : 0.0)
                .addField("max_temperature", temperaturePo.getMaxTemperature() != null ?  Double.parseDouble(temperaturePo.getMaxTemperature()) : 0.0)
                .addField("min_temperature", temperaturePo.getMinTemperature() != null ? Double.parseDouble(temperaturePo.getMinTemperature()) : 0.0)
                .addField("ave_temperature", temperaturePo.getAveTemperature() != null ? Double.parseDouble(temperaturePo.getAveTemperature()) : 0.0)
                .time(temperaturePo.getCreateTime().toInstant(), WritePrecision.S);

        WriteApiBlocking writeApi = client.getWriteApiBlocking();
        writeApi.writePoint(point);

        //  保存实时数据
        Long aLong = alarmTimeStampMap.get(temperaturePo.getHardwareId());
        long timestamp = temperaturePo.getCreateTime().getTime();
        if (aLong != null && aLong > timestamp) {
            temperatureDataService.save(tTemperatureData);
        }

        Boolean flag = saveFlag.get(temperaturePo.getHardwareId());

        // 如果flag为true，则保存到mysql
        if (flag != null && flag) {
            log.info("保存到mysql");
            saveFlag.put(temperaturePo.getHardwareId(), false);
            saveToMysql(tTemperatureData);
        }
    }

    private void saveToMysql(TTemperatureData tTemperatureData) {
        long timestamp = tTemperatureData.getCreateTime().getTime();
        // 前 10min 时间点
        Date start = new Date(timestamp - 1000L * 60 * 10);
        // 当前时间
        Date now = new Date(timestamp);
        // 后 10min 时间点
        Date last = new Date(timestamp + 1000L * 60 * 10);

        CommonQuery commonQuery = new CommonQuery();
        commonQuery.setHardwareId(tTemperatureData.getHardwareId());
        commonQuery.setStartTime(start);
        commonQuery.setEndTime(now);
        List<TTemperatureData> temperatureDataList = temperatureDataService.getTemperatureDataListByInfluxDB(commonQuery);
        if (!temperatureDataList.isEmpty()) {
            temperatureDataService.saveBatch(temperatureDataList);
        }
        alarmTimeStampMap.put(tTemperatureData.getHardwareId(), last.getTime());
    }



    public void processMessage(TiLongTimeDataPo temperaturePo) {
        Integer siteId = temperaturePo.getSiteId();
        Long dataTime = temperaturePo.getDataTime();

        // 处理TI1，左是 LEFT
        processTiArea(temperaturePo, siteId, dataTime, "TI1", MonitorType.TI_LEFT);

        // 处理TI2，右是 RIGHT
        processTiArea(temperaturePo, siteId, dataTime, "TI2", MonitorType.TI_RIGHT);

        // 处理TI1的告警逻辑
        processAlarm(temperaturePo, siteId, "TI1", MonitorType.TI_LEFT);

        // 处理TI2的告警逻辑
        processAlarm(temperaturePo, siteId, "TI2", MonitorType.TI_RIGHT);
    }

    /**
     * 处理TI的每个区域
     */
    private void processTiArea(TiLongTimeDataPo po, Integer siteId, Long dataTime, String tiPrefix, MonitorType monitorType) {
        TiInfoVo tiInfoVo = tCameraInfoService.getTiInfoVoList(siteId, HardwareType.TI.getCode(), monitorType.getType());
        if (tiInfoVo == null) {
            log.warn("未找到TI:{} 区域:{} 的信息", tiPrefix, monitorType.getMonitorLocation());
            return;
        }
        Integer cameraId = tiInfoVo.getCameraId();
        Integer hardwareId = tiInfoVo.getHardwareId();

        for (int area = 1; area <= 10; area++) {
            String activeMethodName = String.format("is%sArea%dTemperatureLongTimeActive", tiPrefix, area);
            String valueMethodName = String.format("get%sArea%dTemperatureLongTime", tiPrefix, area);

            try {
                // 反射获取对应的激活标志
                Method activeMethod = TiLongTimeDataPo.class.getMethod(activeMethodName);
                boolean isActive = (boolean) activeMethod.invoke(po);
                if (!isActive) {
                    continue;
                }

                // 长时间 温度 1小时 1次
                boolean b = shouldRecordThisHour(hardwareId + "_" + tiPrefix + area, dataTime);
                if (!b) {
                    continue;
                }

                // 反射获取对应的温度数据
                Method valueMethod = TiLongTimeDataPo.class.getMethod(valueMethodName);
                Double temperatureValue = (Double) valueMethod.invoke(po);

                // 保存数据
                TLongTimeTemperature temperature = new TLongTimeTemperature();
                temperature.setTemperature(temperatureValue + "");
                temperature.setDateTime(new Date(dataTime));
                temperature.setAreaName("区域" + area);
                temperature.setCameraId(cameraId);
                temperature.setHardwareId(hardwareId);
                tLongTimeTemperatureService.save(temperature);

            } catch (Exception e) {
                log.error("处理{}区域{}数据异常", tiPrefix, area, e);
            }
        }
    }

    /**
     * 处理TI的告警逻辑（检测限值，触发告警和Modbus写入）
     */
    private void processAlarm(TiLongTimeDataPo po, Integer siteId, String tiPrefix, MonitorType monitorType) {
        for (int area = 1; area <= 10; area++) {
            for (int limit = 1; limit <= 3; limit++) {
                String activeMethodName = String.format("is%sArea%dTemperatureLimit%dActive", tiPrefix, area, limit);
                try {

                    Method activeMethod = TiLongTimeDataPo.class.getMethod(activeMethodName);
                    boolean isActive = (boolean) activeMethod.invoke(po);

                    Boolean lastAlarm = alarmFlag.get(siteId + "_" + activeMethodName);

                    // 放入告警 标识
                    alarmFlag.put(siteId + "_" + activeMethodName, isActive);

                    // 如果没有变化，则跳过
                    if (!isActive || (lastAlarm != null && lastAlarm)) {
                        continue;
                    }

                    TiInfoVo tiInfoVo = tCameraInfoService.getTiInfoVoList(siteId, HardwareType.TI.getCode(), monitorType.getType());
                    Integer hardwareId = tiInfoVo.getHardwareId();

                    // 通知 mysql 存储数据
                    saveFlag.put(hardwareId, true);

                    String modbusField = String.format("%sArea%dTemperatureLimit%dSetpoint", tiPrefix, area, limit);
                    String protocolField = String.format("%sArea%dTemperatureLimit%dActiveToPLC", tiPrefix, area, limit);

                    TConfigRule rule = configRuleService.getOne(
                            new LambdaQueryWrapper<TConfigRule>().eq(TConfigRule::getModbusField, modbusField)
                    );
                    TModbusProtocol protocol = modbusProtocolService.getOne(
                            new LambdaQueryWrapper<TModbusProtocol>().eq(TModbusProtocol::getField, protocolField)
                    );

                    String fileName = videoFolder + File.separator + "TI" + System.currentTimeMillis() + ".mp4";
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("fileName", fileName);

                    hkCameraApi.downloadRecordByTime(hardwareId, new Date(po.getDataTime()), new Date(po.getDataTime()), fileName);
                    alarmService.addAlarmInfo(siteId, hardwareId + "", AlarmType.TI_ALARM, rule, new Date(po.getDataTime()), JSON.toJSONString(jsonObject));
                    modbusApi.writeSingleRegisterBit(siteId + "", protocol.getAddress(),true, protocol.getBitIndex());

                    log.info("【TI告警触发】siteId:{} 设备ID:{} 监测点:{} 区域:{} 等级:{}",
                            siteId, hardwareId, tiPrefix, area, limit);

                } catch (Exception e) {
                    log.error("处理{} 区域{} 限值{} 告警异常", tiPrefix, area, limit, e);
                }
            }
        }
    }

    /**
     * 是否需要记录本小时的数据
     */
    private boolean shouldRecordThisHour(String key, Long dataTime) {
        long currentHour = dataTime / (1000 * 60 * 60);  // 时间戳转小时

        Long lastHour = lastRecordedTimeMap.getOrDefault(key, -1L);
        if (currentHour > lastHour) {
            lastRecordedTimeMap.put(key, currentHour);
            return true;
        }
        return false;
    }

    @Scheduled(fixedRate = 1000)
    public void sendMessages() {
        for (String deviceId : messageBuffer.keySet()) {
            executor.submit(() -> {
                try {
                    Map<Long, List<TemperaturePo>> deviceMessages = messageBuffer.get(deviceId);
                    // 找到该机组中最大的时间戳
                    long maxTimestampKey = deviceMessages.keySet().stream().max(Long::compare).orElse(0L);

                    List<Long> keysToRemove = new ArrayList<>();

                    List<JSONObject> resObject = new ArrayList<>();


                    for (Long timestampSec : new ArrayList<>(deviceMessages.keySet())) {
                        if (timestampSec != maxTimestampKey) {

                            List<TemperaturePo> temperatureDataList = deviceMessages.get(timestampSec);
                            Map<Integer, List<TemperaturePo>> ruleIdMap = temperatureDataList.stream().collect(Collectors.groupingBy(TemperaturePo::getRuleId));


                            for (List<TemperaturePo> values : ruleIdMap.values()) {
                                List<String[]> res = new ArrayList<>();
                                for (TemperaturePo value : values) {
                                    String temperature = value.getCurrentTemperature() == null ? value.getMaxTemperature() : value.getCurrentTemperature();
                                    double doubleValue = new BigDecimal(temperature).setScale(2, RoundingMode.HALF_UP).doubleValue();
                                    String time = DateUtil.format(value.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
                                    String[] arr = new String[]{time, doubleValue + ""};
                                    res.add(arr);
                                }
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("ruleId", values.get(0).getRuleId());
                                jsonObject.put("name", values.get(0).getRuleName());
                                jsonObject.put("value", res);
                                resObject.add(jsonObject);
                            }
                            keysToRemove.add(timestampSec);
                        }
                    }

                    // 如果有数据，推送消息
                    if (CollectionUtil.isNotEmpty(resObject)) {
                        // 将数据推送给该机组所有的用户
                        sseService.sendToAllTempClients(deviceId, JSON.toJSONString(resObject));
                    }

                    // 移除所有需要移除的时间戳键
                    for (Long key : keysToRemove) {
                        deviceMessages.remove(key);
                    }
                } catch (Exception e) {
                    log.error("Error processing device {} data", deviceId, e);
                }
            });
        }
    }

    public static void main(String[] args) {
        String URL = "http://192.168.31.180:8086";
        String TOKEN = "x0Cetw3pEiZJu4hVXdQ6yul31NdexqFcCFGbv1BFOP0TJc-8VcaK6k4LitiVOAPnm9k0Fkqp8wZNrx5xbVwqCw==";
        String ORG = "tc";
        String BUCKET = "test";
        try (InfluxDBClient client = InfluxDBClientFactory.create(URL, TOKEN.toCharArray(), ORG, BUCKET)) {


            // Flux 查询语句
            String fluxQuery = "import \"influxdata/influxdb/v1\" \n" +
                    "from(bucket: \"" + BUCKET + "\") " +
                    "|> range(start:2025-02-14T05:58:00.000Z, stop:2025-02-14T05:59:00.000Z) " +
                    "|> filter(fn: (r) => r._measurement == \"temperature\")" +
                    "|> filter(fn: (r) => r.camera_id == \"1\")" +
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
                    int cameraId = Integer.parseInt(map.get("camera_id").toString()) ;
                    int ruleId = (Integer.parseInt(map.get("rule_id").toString())) ;
                    long channelId = (long) map.get("channel_id");
                    long ruleType = (long) map.get("rule_type");
                    String ruleName = (String) map.get("rule_name");
                    double currentTemperature = (double) map.get("current_temperature");
                    double maxTemperature = (double) map.get("max_temperature");
                    double minTemperature = (double) map.get("min_temperature");
                    double aveTemperature = (double) map.get("ave_temperature");

                    data.setCreateTime(Date.from(time));
                    data.setCameraId(cameraId);
                    data.setChannelId((int) channelId);
                    data.setRuleId(ruleId);
                    data.setRuleType((int) ruleType);
                    data.setRuleName(ruleName);
                    data.setCurrentTemperature(currentTemperature + "");
                    data.setMaxTemperature(maxTemperature + "");
                    data.setMinTemperature(minTemperature + "");
                    data.setAveTemperature(aveTemperature + "");

                    temperatureDataList.add(data);
                }
            }

            for (TTemperatureData data : temperatureDataList) {
                String jsonString = JSON.toJSONString(data);
                System.out.println("jsonString = " + jsonString);
            }
        }
    }
}
