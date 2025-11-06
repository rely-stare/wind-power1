package com.tc.modules.hk.service.impl;


import com.alibaba.fastjson.JSON;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.tc.config.RabbitMqConfig;
import com.tc.modules.common.entity.TCameraArea;
import com.tc.modules.common.entity.TModbusProtocol;
import com.tc.modules.common.service.CameraService;
import com.tc.modules.common.service.HardwareService;
import com.tc.modules.common.service.ModbusProtocolService;
import com.tc.modules.hk.entity.DeviceInfoEntity;
import com.tc.modules.hk.entity.TemperatureEntity;
import com.tc.modules.hk.enums.NET_SDK_CALLBACK_TYPE;
import com.tc.modules.hk.netsdk.HCNetSDK;
import com.tc.modules.hk.netsdk.HCNetSDKManager;
import com.tc.modules.hk.service.TemperatureService;
import com.tc.modules.plc.service.ModbusService;
import com.tc.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TemperatureServiceImpl implements TemperatureService {

    // 将对象存储到 Map 中，防止被 jvm 回收
    private final Map<String, HCNetSDK.FRemoteConfigCallBack> callbackMap = new HashMap<>();

    private final Map<Integer,List<TCameraArea>> cameraAreaMap = new HashMap<>();

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private HardwareService hardwareService;

    @Autowired
    private CameraService cameraService;

    @Autowired
    private ModbusProtocolService modbusProtocolService;

    @Autowired
    private ModbusService modbusService;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor executor;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private HCNetSDKManager hCNetSDKManager;


    @Override
    public void startTemperature(int userId, DeviceInfoEntity deviceInfo) {

        // 获取 SDK 实例
        HCNetSDK sdkInstance = hCNetSDKManager.getSDKInstance();

        // 开启测温
        startRealTimeThermometry(sdkInstance, userId, deviceInfo);

    }

    @PostConstruct
    public void init() {
        Map<Integer, List<TCameraArea>> listMap = cameraService.list().stream().collect(Collectors.groupingBy(TCameraArea::getCameraId));
        for (Integer i : listMap.keySet()) {
            cameraAreaMap.put(i, listMap.get(i));
        }
    }

    public void startRealTimeThermometry(HCNetSDK hCNetSDK, int userID, DeviceInfoEntity deviceInfo) {

        HCNetSDK.NET_DVR_REALTIME_THERMOMETRY_COND cond = new HCNetSDK.NET_DVR_REALTIME_THERMOMETRY_COND();
        cond.read();
        cond.dwSize = cond.size();  // 获取结构体大小
        cond.dwChan = 2;      // 设置通道号为2
        cond.byRuleID = 0;    // 设置规则ID为1
        cond.byMode = 1;      // 设置为定时模式
        cond.wInterval = 2;  // 设置上传间隔为3600秒
        cond.write();

        // 设置用户数据
        Pointer pUserData = new Memory(37);
        pUserData.setString(0, deviceInfo.getIp());
        pUserData.setString(16, String.valueOf(deviceInfo.getCameraId()));

        // 定义回调函数
        HCNetSDK.FRemoteConfigCallBack callback = createCallback(String.valueOf(deviceInfo.getCameraId()), deviceInfo.getHardwareId());
        callbackMap.put(deviceInfo.getIp(), callback);

        // 启动实时测温
        int lHandle = hCNetSDK.NET_DVR_StartRemoteConfig(userID, HCNetSDK.NET_DVR_GET_REALTIME_THERMOMETRY, cond.getPointer(), cond.size(), callback, pUserData);

        if (lHandle < 0) {
            log.error("NET_DVR_GET_REALTIME_THERMOMETRY failed, error code: {}", hCNetSDK.NET_DVR_GetLastError());
        } else {
            log.info("NET_DVR_GET_REALTIME_THERMOMETRY is successful!");
        }

    }

    private HCNetSDK.FRemoteConfigCallBack createCallback(String cameraId,  int hardwareId) {
        return (dwType, lpBuffer, dwBufLen, pUserData) -> {
            // 复制 dwType
            final int copiedDwType = dwType;

            // 复制 lpBuffer 数据
            final byte[] copiedLpBuffer;
            if (lpBuffer != null && dwBufLen > 0) {
                copiedLpBuffer = new byte[dwBufLen];
                lpBuffer.read(0, copiedLpBuffer, 0, dwBufLen);
            } else {
                copiedLpBuffer = new byte[0];
            }

            // 复制 pUserData 数据
//            final String deviceId = pUserData.getString(16);;

            // 提交到线程池
            executorService.submit(() -> {
                try {
                    NET_SDK_CALLBACK_TYPE callbackType = NET_SDK_CALLBACK_TYPE.fromValue(copiedDwType);
                    switch (callbackType) {
                        case NET_SDK_CALLBACK_TYPE_STATUS:
                            handleStatusCallback(copiedLpBuffer);
                            break;
                        case NET_SDK_CALLBACK_TYPE_PROGRESS:
                            handleProgressCallback(copiedLpBuffer);
                            break;
                        case NET_SDK_CALLBACK_TYPE_DATA:
                            handleDataCallback(copiedLpBuffer, cameraId, hardwareId);
                            break;
                        default:
                            log.debug("未知回调类型: {}", copiedDwType);
                    }
                } catch (Exception e) {
                    log.error("回调处理异常: ", e);
                }
            });
        };
    }

    // 处理状态回调
    private void handleStatusCallback(byte[] lpBuffer) {
        ByteBuffer buffer = ByteBuffer.wrap(lpBuffer).order(ByteOrder.LITTLE_ENDIAN);
        int dwStatus = buffer.getInt();  // 解析状态值
        log.debug("回调类型: 状态回调，状态值: {}", dwStatus);
    }

    // 处理进度回调
    private void handleProgressCallback(byte[] lpBuffer) {
        ByteBuffer buffer = ByteBuffer.wrap(lpBuffer).order(ByteOrder.LITTLE_ENDIAN);
        int progress = buffer.getInt();  // 解析进度值
        log.debug("回调类型: 进度回调，进度值: {}%", progress);
    }

    // 处理数据回调
    private void handleDataCallback(byte[] lpBuffer, String deviceId, int hardwareId) {

        Date date = new Date();
        HCNetSDK.NET_DVR_THERMOMETRY_UPLOAD thermometryData = new HCNetSDK.NET_DVR_THERMOMETRY_UPLOAD();
        thermometryData.write();
        Pointer pShipAlarm = thermometryData.getPointer();
        pShipAlarm.write(0, lpBuffer, 0, thermometryData.size()); // 复制数据
        thermometryData.read();

        TemperatureEntity temperature = new TemperatureEntity();
        temperature.setHardwareId(hardwareId);
        temperature.setCameraId(Integer.parseInt(deviceId));
        temperature.setChannelId(2);
        temperature.setRuleType(thermometryData.byRuleCalibType);
        temperature.setRuleId(thermometryData.byRuleID);
        try {
            temperature.setRuleName(new String(thermometryData.szRuleName, "GBK").trim());
        } catch (Exception e) {
            log.error("规则名称解析错误", e);
        }

        if (thermometryData.byRuleCalibType == 0) {
            temperature.setCurrentTemperature(String.valueOf(thermometryData.struPointThermCfg.fTemperature));
        } else {
            temperature.setAveTemperature(String.valueOf(thermometryData.struLinePolygonThermCfg.fAverageTemperature));
            temperature.setMaxTemperature(String.valueOf(thermometryData.struLinePolygonThermCfg.fMaxTemperature));
            temperature.setMinTemperature(String.valueOf(thermometryData.struLinePolygonThermCfg.fMinTemperature));
        }

        redisUtils.setCacheObject("iot:device-status:temperature:" + deviceId , true, 10, TimeUnit.SECONDS);

        temperature.setCreateTime(date);

        log.debug("温度数据 {}", temperature.toString());

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.TEMPERATURE_DATA_EXCHANGE,
                RabbitMqConfig.TEMPERATURE_ROUTING_KEY,
                JSON.toJSONString(temperature)
        );

        send2Modbus(temperature);
    }

    public void send2Modbus(TemperatureEntity temperature) {
        executor.execute(()->{
            int ruleId = temperature.getRuleId();
            int cameraId = temperature.getCameraId();
            List<TCameraArea> tCameraAreas = cameraAreaMap.get(cameraId);

            // 根据规则 找到对应区域
            TCameraArea tCameraArea = tCameraAreas.stream().filter(s -> s.getRuleId() == ruleId).findFirst().orElse(null);
            if (tCameraArea != null) {
                String modbusFiled = tCameraArea.getModbusField();
                TModbusProtocol modbusProtocolByCache = modbusProtocolService.getModbusProtocolCacheByfiled(modbusFiled);

                String temp = temperature.getCurrentTemperature() == null ? temperature.getMaxTemperature() : temperature.getCurrentTemperature();
                float floatValue = BigDecimal.valueOf(Float.parseFloat(temp)).setScale(2, RoundingMode.HALF_UP).floatValue();

                String type = modbusProtocolByCache.getType();

                try {
                    if ("real".equalsIgnoreCase(type)) {
                        // 设备暂时写死
                        long l = System.currentTimeMillis();
                        modbusService.writeSingleRegisterReal(1 + "", modbusProtocolByCache.getAddress(), floatValue);
                        log.debug("写入modbus成功,耗时: {}ms", System.currentTimeMillis() - l);
                    }
                } catch (Exception e) {
                    log.error("写入modbus异常", e);
                }
            }
        });
    }
}
