package com.tc.modules.message.rabbitmq;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.rabbitmq.client.Channel;
import com.tc.common.enums.AlarmType;
import com.tc.modules.api.ModbusApi;
import com.tc.modules.entity.TConfigRule;
import com.tc.modules.entity.TModbusProtocol;
import com.tc.modules.entity.TSpeedData;
import com.tc.modules.entity.TSpeedFFTActive;
import com.tc.modules.message.sse.service.SseService;
import com.tc.modules.message.websocket.WebSocketServer;
import com.tc.modules.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.listener.enabled", havingValue = "true")
public class SpeedMessageReceiver {

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private SseService sseService;

    @Autowired
    private InfluxDBClient client;

    @Autowired
    private ModbusApi modbusApi;

    @Autowired
    private TConfigRuleService configRuleService;

    @Autowired
    private TModbusProtocolService modbusProtocolService;

    @Autowired
    private TAlarmService alarmService;

    @Autowired
    private TSpeedDataService speedDataService;

    @Autowired
    private TSpeedFFTActiveService speedFFTActiveService;

    @Autowired
    @Qualifier("threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    // deviceId -> second -> List<JSONObject>
    private final Map<String, Map<Long, List<JSONObject>>> messageBuffer = new ConcurrentHashMap<>();

    private final Map<String, Long> alarmTimeStampMap = new HashMap<>();

    private static final Map<String, Boolean> alarmFlag = new HashMap<>();


    @RabbitListener(queues = "speed_data_queue", ackMode = "MANUAL")
    public void onMessage(String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            // 获取机组ID
            String deviceId = jsonObject.getString("unit");
            // 时间戳
            long timestamp = jsonObject.getLong("timestamp");
            // 获取秒级时间戳
            long second = timestamp / 1000;
            // 根据机组ID来分组存储消息
            messageBuffer
                    .computeIfAbsent(deviceId, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(second, k -> new ArrayList<>())
                    .add(new JSONObject(jsonObject));

            processMessage(jsonObject);

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

    public void processMessage(JSONObject jsonObject) {
//        log.info("Received speed message: {}", jsonObject);

        saveToInfluxDB(jsonObject);

        String deviceId = jsonObject.getString("unit");
        Long timestamp = jsonObject.getLong("timestamp");
        Boolean bGeneratorSpeedFFTActive = jsonObject.getBoolean("bGeneratorSpeedFFTActive");
        Boolean bGeneratorSpeedLimit1Active = jsonObject.getBoolean("bGeneratorSpeedLimit1Active");
        Boolean bGeneratorSpeedLimit2Active = jsonObject.getBoolean("bGeneratorSpeedLimit2Active");
        Boolean bGeneratorSpeedLimit3Active = jsonObject.getBoolean("bGeneratorSpeedLimit3Active");
        Boolean bGeneratorSpeedFluctuate1Active = jsonObject.getBoolean("bGeneratorSpeedFluctuate1Active");
        Boolean bGeneratorSpeedFluctuate2Active = jsonObject.getBoolean("bGeneratorSpeedFluctuate2Active");
        Boolean bGeneratorSpeedFluctuate3Active = jsonObject.getBoolean("bGeneratorSpeedFluctuate3Active");


        Boolean bGeneratorSpeedFFTActiveKeep = alarmFlag.getOrDefault(deviceId + "_" + bGeneratorSpeedFFTActive, false);
        Boolean bGeneratorSpeedLimit1ActiveKeep = alarmFlag.getOrDefault(deviceId + "_" + bGeneratorSpeedLimit1Active, false);
        Boolean bGeneratorSpeedLimit2ActiveKeep = alarmFlag.getOrDefault(deviceId + "_" + bGeneratorSpeedLimit2Active, false);
        Boolean bGeneratorSpeedLimit3ActiveKeep = alarmFlag.getOrDefault(deviceId + "_" + bGeneratorSpeedLimit3Active, false);
        Boolean bGeneratorSpeedFluctuate1ActiveKeep = alarmFlag.getOrDefault(deviceId + "_" + bGeneratorSpeedFluctuate1Active, false);
        Boolean bGeneratorSpeedFluctuate2ActiveKeep = alarmFlag.getOrDefault(deviceId + "_" + bGeneratorSpeedFluctuate2Active, false);
        Boolean bGeneratorSpeedFluctuate3ActiveKeep = alarmFlag.getOrDefault(deviceId + "_" + bGeneratorSpeedFluctuate3Active, false);


        alarmFlag.put(deviceId + "_" + bGeneratorSpeedFFTActive, bGeneratorSpeedFFTActive);
        alarmFlag.put(deviceId + "_" + bGeneratorSpeedLimit1Active, bGeneratorSpeedLimit1Active);
        alarmFlag.put(deviceId + "_" + bGeneratorSpeedLimit2Active, bGeneratorSpeedLimit2Active);
        alarmFlag.put(deviceId + "_" + bGeneratorSpeedLimit3Active, bGeneratorSpeedLimit3Active);
        alarmFlag.put(deviceId + "_" + bGeneratorSpeedFluctuate1Active, bGeneratorSpeedFluctuate1Active);
        alarmFlag.put(deviceId + "_" + bGeneratorSpeedFluctuate2Active, bGeneratorSpeedFluctuate2Active);
        alarmFlag.put(deviceId + "_" + bGeneratorSpeedFluctuate3Active, bGeneratorSpeedFluctuate3Active);


        Long aLong = alarmTimeStampMap.get(deviceId);
        if (aLong != null && aLong > timestamp) {
            long start2 = System.currentTimeMillis();
            TSpeedData speedData = new TSpeedData();
            speedData.setSiteId(Integer.parseInt(deviceId));
            speedData.setCreateTime(new Date(timestamp));
            speedData.setSpeed(jsonObject.getDouble("rGeneratorSpeed"));
            speedData.setSpeedDelay(jsonObject.getDouble("rGeneratorSpeedDelay"));
            speedData.setSpeedFluctuateDelay(jsonObject.getDouble("rGeneratorSpeedFluctuateDelay"));
            speedData.setSpeedAverageDelay(jsonObject.getDouble("rGeneratorSpeedAverageDelay"));
            speedDataService.save(speedData);
            log.debug("保存数据 mysql 耗时：{} ms", System.currentTimeMillis() - start2);
        }

        // 发电机转速频谱分析触发
        if (bGeneratorSpeedFFTActive && !bGeneratorSpeedFFTActiveKeep) {
            // 读取前 10min 的数据 并存储 mysql
            Date start = new Date(timestamp - 1000 * 60 * 10);
            Date end = new Date(timestamp);
            // 记录最后一条数据的时间
            alarmTimeStampMap.put(deviceId, end.getTime());
            List<TSpeedData> speedDataByInfluxDB = speedDataService.getSpeedDataByInfluxDB(Integer.parseInt(deviceId), start, end);
            speedDataService.saveBatch(speedDataByInfluxDB);

            // 记录上升沿触发记录
            speedDataService.frequencyAnalyse(speedDataByInfluxDB, Integer.parseInt(deviceId), new Date(timestamp));
        }

        // 发电机转速一级超速触发
        if (bGeneratorSpeedLimit1Active && !bGeneratorSpeedLimit1ActiveKeep) {
            TConfigRule rule = configRuleService.getOne(new LambdaQueryWrapper<TConfigRule>().eq(TConfigRule::getModbusField, "GeneratorSpeedLimit1Setpoint"));
            TModbusProtocol protocol = modbusProtocolService.getOne(new LambdaQueryWrapper<TModbusProtocol>().eq(TModbusProtocol::getField, "bGeneratorSpeedLimit1ActiveToPLC"));
            alarmService.addAlarmInfo(Integer.parseInt(deviceId), null, AlarmType.SPEED_ALARM, rule, new Date(timestamp));
            modbusApi.writeSingleRegisterBit(deviceId, protocol.getAddress(),true, protocol.getBitIndex());
            saveToMysql(timestamp, deviceId,5);

        }
        // 发电机转速二级超速触发
        if (bGeneratorSpeedLimit2Active  && !bGeneratorSpeedLimit2ActiveKeep) {
            TConfigRule rule = configRuleService.getOne(new LambdaQueryWrapper<TConfigRule>().eq(TConfigRule::getModbusField, "GeneratorSpeedLimit2Setpoint"));
            TModbusProtocol protocol = modbusProtocolService.getOne(new LambdaQueryWrapper<TModbusProtocol>().eq(TModbusProtocol::getField, "bGeneratorSpeedLimit2ActiveToPLC"));
            alarmService.addAlarmInfo(Integer.parseInt(deviceId), null, AlarmType.SPEED_ALARM, rule,new Date(timestamp));
            modbusApi.writeSingleRegisterBit(deviceId, protocol.getAddress(),true, protocol.getBitIndex());

            saveToMysql(timestamp, deviceId,5);

        }
        // 发电机转速三级超速触发
        if (bGeneratorSpeedLimit3Active  && !bGeneratorSpeedLimit3ActiveKeep) {
            TConfigRule rule = configRuleService.getOne(new LambdaQueryWrapper<TConfigRule>().eq(TConfigRule::getModbusField, "GeneratorSpeedLimit3Setpoint"));
            TModbusProtocol protocol = modbusProtocolService.getOne(new LambdaQueryWrapper<TModbusProtocol>().eq(TModbusProtocol::getField, "bGeneratorSpeedLimit3ActiveToPLC"));
            alarmService.addAlarmInfo(Integer.parseInt(deviceId), null, AlarmType.SPEED_ALARM, rule,new Date(timestamp));
            modbusApi.writeSingleRegisterBit(deviceId, protocol.getAddress(),true, protocol.getBitIndex());
            saveToMysql(timestamp, deviceId,5);

        }
        // 转速稳定性一级比例触发
        if (bGeneratorSpeedFluctuate1Active && !bGeneratorSpeedFluctuate1ActiveKeep) {

            TConfigRule rule = configRuleService.getOne(new LambdaQueryWrapper<TConfigRule>().eq(TConfigRule::getModbusField, "GeneratorSpeedFluctuate1Setpoint"));
            TModbusProtocol protocol = modbusProtocolService.getOne(new LambdaQueryWrapper<TModbusProtocol>().eq(TModbusProtocol::getField, "bGeneratorSpeedFluctuate1ActiveToPLC"));
            alarmService.addAlarmInfo(Integer.parseInt(deviceId), null, AlarmType.STABILITY_ALARM, rule,new Date(timestamp));
            modbusApi.writeSingleRegisterBit(deviceId, protocol.getAddress(),true, protocol.getBitIndex());
            saveToMysql(timestamp, deviceId,2);
        }
        // 转速稳定性二级比例触发
        if (bGeneratorSpeedFluctuate2Active && !bGeneratorSpeedFluctuate2ActiveKeep) {
            TConfigRule rule = configRuleService.getOne(new LambdaQueryWrapper<TConfigRule>().eq(TConfigRule::getModbusField, "GeneratorSpeedFluctuate2Setpoint"));
            TModbusProtocol protocol = modbusProtocolService.getOne(new LambdaQueryWrapper<TModbusProtocol>().eq(TModbusProtocol::getField, "bGeneratorSpeedFluctuate2ActiveToPLC"));
            alarmService.addAlarmInfo(Integer.parseInt(deviceId), null, AlarmType.STABILITY_ALARM, rule,new Date(timestamp));
            modbusApi.writeSingleRegisterBit(deviceId, protocol.getAddress(), true,protocol.getBitIndex());
            saveToMysql(timestamp, deviceId,2);
        }
        // 转速稳定性三级比例触发
        if (bGeneratorSpeedFluctuate3Active && !bGeneratorSpeedFluctuate3ActiveKeep) {
            TConfigRule rule = configRuleService.getOne(new LambdaQueryWrapper<TConfigRule>().eq(TConfigRule::getModbusField, "GeneratorSpeedFluctuate3Setpoint"));
            TModbusProtocol protocol = modbusProtocolService.getOne(new LambdaQueryWrapper<TModbusProtocol>().eq(TModbusProtocol::getField, "bGeneratorSpeedFluctuate3ActiveToPLC"));
            alarmService.addAlarmInfo(Integer.parseInt(deviceId), null, AlarmType.STABILITY_ALARM, rule,new Date(timestamp));
            modbusApi.writeSingleRegisterBit(deviceId, protocol.getAddress(),true, protocol.getBitIndex());
            saveToMysql(timestamp, deviceId,2);
        }
    }

    private void saveToInfluxDB(JSONObject jsonObject) {
        String deviceId = jsonObject.getString("unit");
        Long timestamp = jsonObject.getLong("timestamp");
        long start1 = System.currentTimeMillis();
        Point point = Point
                .measurement("speed_data")
                .addTag("device_id", deviceId)  // 确保是字符串
                .addField("rGeneratorSpeed", jsonObject.getDouble("rGeneratorSpeed"))
                .addField("rGeneratorSpeedDelay", jsonObject.getDouble("rGeneratorSpeedDelay"))
                .addField("rGeneratorSpeedFluctuateDelay", jsonObject.getDouble("rGeneratorSpeedFluctuateDelay"))
                .addField("rGeneratorSpeedAverageDelay", jsonObject.getDouble("rGeneratorSpeedAverageDelay"))
                .time(new Date(timestamp).getTime(), WritePrecision.MS);

        WriteApiBlocking writeApi = client.getWriteApiBlocking();
        writeApi.writePoint(point);
        log.debug("保存数据 influxDB 耗时：{} ms", System.currentTimeMillis() - start1);
    }

    private void saveToMysql(Long timestamp, String deviceId, int min) {
        // 前 5min 时间点
        Date start = new Date(timestamp - 1000L * 60 * min);
        // 当前时间
        Date now = new Date(timestamp);
        // 后 5min 时间点
        Date last = new Date(timestamp + 1000L * 60 * min);

        Long alarmTime = alarmTimeStampMap.get(deviceId);

        if (alarmTime == null || now.getTime() - alarmTime > 1000L * 60 * min) {
            alarmTimeStampMap.put(deviceId, last.getTime());
            List<TSpeedData> speedDataByInfluxDB = speedDataService.getSpeedDataByInfluxDB(Integer.parseInt(deviceId), start, now);
            speedDataService.saveBatch(speedDataByInfluxDB);
        }
        // r如果告警时间大于当前时间，将最后的数据时间点 向后 5min
        else if(alarmTime > now.getTime()){
            alarmTimeStampMap.put(deviceId, last.getTime());
        }
        // 如果当前时间 - 上次报警时间 小于 5min，
        else if(now.getTime() - alarmTime <  1000 * 60 * 5){
            alarmTimeStampMap.put(deviceId, last.getTime());
            Date startTine = new Date(now.getTime() - alarmTime);
            List<TSpeedData> speedDataByInfluxDB = speedDataService.getSpeedDataByInfluxDB(Integer.parseInt(deviceId), startTine, now);
            speedDataService.saveBatch(speedDataByInfluxDB);
        }

    }

    @Scheduled(fixedRate = 1000)
    public void sendMessages() {
        // 遍历所有机组的消息
        for (String deviceId : messageBuffer.keySet()) {
            executor.submit(() -> {
                try {
                    Map<Long, List<JSONObject>> deviceMessages = messageBuffer.get(deviceId);

                    // 找到该机组中最大的时间戳
                    long maxTimestampKey = deviceMessages.keySet().stream().max(Long::compare).orElse(0L);

                    // 准备推送数据
                    List<Map<String, Object>> speedObject = new ArrayList<>();
                    List<List<Map<String, Object>>> speedFluctuateObject = new ArrayList<>();
                    List<List<Map<String, Object>>> speedAverageDelayObject = new ArrayList<>();

                    // 用于清理过期的历史数据
                    List<Long> keysToRemove = new ArrayList<>();
                    List<String[]> speedArr = new ArrayList<>();
                    List<String[]> speedDelayArr = new ArrayList<>();
                    List<String[]> speedAverageDelayArr = new ArrayList<>();
                    List<String[]> fluctuateDelayArr = new ArrayList<>();

                    for (Long timestampSec : new ArrayList<>(deviceMessages.keySet())) {
                        if (timestampSec != maxTimestampKey) {
                            List<JSONObject> currentSecondMessages = deviceMessages.get(timestampSec);
                            if (currentSecondMessages != null && !currentSecondMessages.isEmpty()) {

                                for (JSONObject value : currentSecondMessages) {
                                    Long timestamp = value.getLong("timestamp");
                                    String time = DateUtil.format(new Date(timestamp), "yyyy-MM-dd HH:mm:ss.SSS");
                                    Double speed = value.getDouble("rGeneratorSpeed");
                                    Double speedDelay = value.getDouble("rGeneratorSpeedDelay");
                                    Double speedAverageDelay = value.getDouble("rGeneratorSpeedAverageDelay");
                                    Double fluctuateDelay = value.getDouble("rGeneratorSpeedFluctuateDelay");

                                    speedArr.add(new String[]{time, speed + ""});
                                    speedDelayArr.add(new String[]{time, speedDelay + ""});
                                    speedAverageDelayArr.add(new String[]{time, speedAverageDelay + ""});
                                    fluctuateDelayArr.add(new String[]{time, fluctuateDelay + ""});
                                }
                            }

                            // 添加需要移除的时间戳
                            keysToRemove.add(timestampSec);
                        }
                    }

                    Map<String,Object> speedMap = new HashMap<>();
                    speedMap.put("name", "speed");
                    speedMap.put("site", deviceId);
                    speedMap.put("value", speedArr);

                    Map<String,Object> fluctuationDataMap = new HashMap<>();
                    fluctuationDataMap.put("site", deviceId);
                    fluctuationDataMap.put("name", "fluctuateDelay");
                    fluctuationDataMap.put("value", fluctuateDelayArr);

                    Map<String,Object> speedDelayMap = new HashMap<>();
                    speedDelayMap.put("site", deviceId);
                    speedDelayMap.put("name", "speedDelay");
                    speedDelayMap.put("value", speedDelayArr);

                    Map<String,Object> averageDelayDataMap = new HashMap<>();
                    averageDelayDataMap.put("site", deviceId);
                    averageDelayDataMap.put("name", "averageDelay");
                    averageDelayDataMap.put("value", speedAverageDelayArr);

                    speedObject.add(speedMap);
                    speedFluctuateObject.add(Arrays.asList(fluctuationDataMap, speedDelayMap));
                    speedAverageDelayObject.add(Arrays.asList(speedDelayMap, averageDelayDataMap));

                    // 推送数据
                    if (CollectionUtil.isNotEmpty(speedObject)) {
//                        webSocketServer.sendToDeviceUsers(deviceId, JSON.toJSONString(speedObject));
                        List<Map<String, Object>> mapList = speedObject.stream()
                                .filter(o -> o.get("value") != null && !((List<?>) o.get("value")).isEmpty())
                                .collect(Collectors.toList());

                        if (!mapList.isEmpty()) {
                            sseService.sendToAllSpeedClients(deviceId, JSON.toJSONString(mapList));
                        }
                    }
                    if (CollectionUtil.isNotEmpty(speedFluctuateObject)) {
                        List<List<Map<String, Object>>> resultList = new ArrayList<>();

                        for (List<Map<String, Object>> o : speedFluctuateObject) {
                            List<Map<String, Object>> mapList = o.stream().filter(o1 -> o1.get("value") != null && !((List<?>) o1.get("value")).isEmpty()).collect(Collectors.toList());
                            resultList.add(mapList);
                        }
                        if (!resultList.isEmpty() && !resultList.get(0).isEmpty()) {
                            sseService.sendToAllFluctuateClients(deviceId, JSON.toJSONString(resultList));
                        }

                    }
                    if (CollectionUtil.isNotEmpty(speedAverageDelayObject)) {
                        List<List<Map<String, Object>>> resultList = new ArrayList<>();

                        for (List<Map<String, Object>> o : speedAverageDelayObject) {
                            List<Map<String, Object>> mapList = o.stream().filter(o1 -> o1.get("value") != null && !((List<?>) o1.get("value")).isEmpty()).collect(Collectors.toList());
                            resultList.add(mapList);
                        }
                        if (!resultList.isEmpty() && !resultList.get(0).isEmpty()) {
                            sseService.sendToAllAverageClients(deviceId, JSON.toJSONString(resultList));
                        }
                    }

                    // 清理掉过期的时间戳数据
                    for (Long key : keysToRemove) {
                        deviceMessages.remove(key);
                    }

                } catch (Exception e) {
                    log.error("Error processing device {} data", deviceId, e);
                }
            });
        }
    }

}
