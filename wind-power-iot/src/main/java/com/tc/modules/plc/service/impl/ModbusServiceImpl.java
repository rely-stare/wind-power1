package com.tc.modules.plc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.tc.common.exception.BusinessException;
import com.tc.config.RabbitMqConfig;
import com.tc.modules.common.entity.TSpeedInfo;
import com.tc.modules.common.service.SpeedInfoService;
import com.tc.modules.plc.client.ModbusTcpClient;
import com.tc.modules.plc.entity.ModbusClientEntity;
import com.tc.modules.plc.handler.ConnectionListener;
import com.tc.modules.plc.pdu.*;
import com.tc.modules.plc.service.ModbusService;
import com.tc.utils.ModbusUtil;
import com.tc.utils.RedisUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


@Service
@Slf4j
public class ModbusServiceImpl implements ModbusService {

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
    // 任务映射
    private final Map<String, ScheduledFuture<?>> speedTaskMap = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> temperatureTaskMap = new ConcurrentHashMap<>();
    // 客户端映射
    public static final Map<String, ModbusTcpClient> clientMap = new ConcurrentHashMap<>();

    private static final int AREA_COUNT = 10;

    @Value("${modbus.read-offset}")
    private int offset;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SpeedInfoService speedInfoService;

    @Autowired
    private RedisUtils redisUtils;


    @Override
    public boolean ReadSingRegisterBit(String deviceCode, int address, int bitIndex) throws Exception {
        ModbusTcpClient client = clientMap.get(deviceCode);
        if (client == null) {
            throw new BusinessException("设备不在线!");
        }
        ReadHoldingRegistersRequest readHoldingRegistersRequest = new ReadHoldingRegistersRequest(offset + address, 1);
        ReadHoldingRegistersResponse readHoldingRegistersResponse = client.readHoldingRegisters(readHoldingRegistersRequest);
        short[] registers = readHoldingRegistersResponse.getRegisters();
        return ModbusUtil.getBitStatus(registers[0], bitIndex);
    }

    @Override
    public float ReadSingRegisterReal(String deviceCode, int address) throws Exception {
        ModbusTcpClient client = clientMap.get(deviceCode);
        if (client == null) {
            throw new BusinessException("设备不在线!");
        }
        ReadHoldingRegistersRequest readHoldingRegistersRequest = new ReadHoldingRegistersRequest(offset + address, 2);
        ReadHoldingRegistersResponse readHoldingRegistersResponse = client.readHoldingRegisters(readHoldingRegistersRequest);
        short[] registers = readHoldingRegistersResponse.getRegisters();
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();
        float rGeneratorSpeed = getFloatFromRegisters(registers[0] & 0xFFFF, registers[1] & 0xFFFF, byteBuf);
        byteBuf.release();
        return rGeneratorSpeed;
    }

    @Override
    public ReadHoldingRegistersResponse ReadHoldingRegisters(String deviceCode, int address, int count) throws Exception {
        ModbusTcpClient client = clientMap.get(deviceCode);
        if (client == null) {
            throw new BusinessException("设备不在线!");
        }
        ReadHoldingRegistersRequest readHoldingRegistersRequest = new ReadHoldingRegistersRequest(offset + address, count);
        return client.readHoldingRegisters(readHoldingRegistersRequest);
    }

    @Override
    public WriteSingleRegisterResponse writeSingleRegister(String deviceCode, int address, int value) throws Exception {
        ModbusTcpClient client = clientMap.get(deviceCode);
        if (client == null) {
            throw new BusinessException("设备不在线!");
        }
        WriteSingleRegisterRequest writeSingleRegisterRequest = new WriteSingleRegisterRequest(address, value);
        return client.writeSingleRegister(writeSingleRegisterRequest);
    }

    @Override
    public WriteMultipleRegistersResponse writeSingleRegisterReal(String deviceCode, int address, float value) throws Exception {
        ModbusTcpClient client = clientMap.get(deviceCode);
        if (client == null) {
            throw new BusinessException("设备不在线!");
        }
        WriteMultipleRegistersResponse writeMultipleRegistersResponse = client.writeMultipleRegisters(address, new float[]{value}, true);
        return writeMultipleRegistersResponse;
    }

    @Override
    public WriteSingleRegisterResponse writeSingleRegisterBit(String deviceCode, int address, boolean value, int bitIndex) throws Exception {
        ModbusTcpClient client = clientMap.get(deviceCode);
        if (client == null) {
            throw new BusinessException("设备不在线!");
        }
        ReadHoldingRegistersRequest readHoldingRegistersRequest = new ReadHoldingRegistersRequest(address, 1);
        ReadHoldingRegistersResponse readHoldingRegistersResponse = client.readHoldingRegisters(readHoldingRegistersRequest);
        short[] registers = readHoldingRegistersResponse.getRegisters();
        int resultValue = ModbusUtil.writeBit(registers[0], bitIndex, value ? 1 : 0);
        WriteSingleRegisterRequest writeSingleRegisterRequest = new WriteSingleRegisterRequest(address, resultValue);
        return client.writeSingleRegister(writeSingleRegisterRequest);

    }

    @Override
    public void init() {
        List<ModbusClientEntity> clinetList = getClinetList();
        for (ModbusClientEntity entity : clinetList) {
            try {
                connect(entity);
            } catch (Exception e) {
                log.error("连接设备 {}:{} 失败", entity.getHost(), entity.getPort(), e);
            }
        }
    }

    @Override
    public void connect(ModbusClientEntity entity) throws Exception {
        ModbusTcpClient tcpClient = clientMap.get(entity.getDeviceCode());
        if (tcpClient != null) {
            throw new BusinessException("设备已连接，不能重复建立连接！");
        }

        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(4);
        // 初始化客户端
        ModbusTcpClient client = ModbusTcpClient.create(entity.getHost(), entity.getPort(), entity.getSlaveId());

        // 添加连接监听器
        client.addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnect() {
                log.info("设备 {}:{} 连接成功", entity.getHost(), entity.getPort());

                // 将客户端添加到客户端映射中
                clientMap.put(entity.getDeviceCode(), client);


                // 合并后的请求：读取 offset 开始的 19 个寄存器（包含 common 和 speed 所需）
                ReadHoldingRegistersRequest mergedRequest = new ReadHoldingRegistersRequest(offset, 19);
                // 合并读取温度与超温数据
                ReadHoldingRegistersRequest mergedTemperatureRequest = new ReadHoldingRegistersRequest(offset + 50, 55);


                // 读取转速信息
                ScheduledFuture<?> future = executorService.scheduleWithFixedDelay((() -> {
                    try {
                        long start = System.currentTimeMillis();
                        ReadHoldingRegistersResponse mergedResponse = client.readHoldingRegisters(mergedRequest);
                        long end = System.currentTimeMillis();
                        log.debug("读取 common+speed 数据耗时: {} ms", end - start);

                        short[] allRegisters = mergedResponse.getRegisters();

                        // 提取 common 数据（前1个）
                        short[] commonRegisters = Arrays.copyOfRange(allRegisters, 0, 1);
                        processCommonData(entity.getDeviceCode(), commonRegisters, System.currentTimeMillis(), byteBuf);

                        // 提取 speed 数据（第11个开始，共9个）
                        short[] speedRegisters = Arrays.copyOfRange(allRegisters, 10, 19);
                        processSpeedData(entity.getDeviceCode(), speedRegisters, System.currentTimeMillis(), byteBuf);

                    } catch (Exception e) {
                        log.error("读取转速数据失败: {}", e.getMessage());
                    }
                }), 0, 1000, TimeUnit.MILLISECONDS);

                speedTaskMap.put(entity.getDeviceCode(), future);


                // 读取温度信息
               ScheduledFuture<?> future2 = executorService.scheduleWithFixedDelay((() -> {
                   try {
                       long start = System.currentTimeMillis();

                       // 一次性读取温度和超温数据
                       ReadHoldingRegistersResponse mergedResponse = client.readHoldingRegisters(mergedTemperatureRequest);
                       short[] allRegisters = mergedResponse.getRegisters();
                       long end = System.currentTimeMillis();
                       log.debug("读取温度和超温数据总耗时: {} ms", end - start);

                       // 拆分温度与超温数据
                       short[] temperatureLongTimeRegisters = Arrays.copyOfRange(allRegisters, 0, 40);
                       short[] activeRegisters = Arrays.copyOfRange(allRegisters, 40, 55);

                       processTemperatureData(entity.getDeviceCode(), temperatureLongTimeRegisters, activeRegisters, System.currentTimeMillis());
                       // 设置状态缓存
                       redisUtils.setCacheObject("iot:device-status:speed:" + entity.getDeviceCode(), true, 10, TimeUnit.SECONDS);
                   } catch (Exception e) {
                       log.error("读取温度数据失败: {}", e.getMessage());
                   }
               }), 0, 1000, TimeUnit.MILLISECONDS);

               temperatureTaskMap.put(entity.getDeviceCode(), future2);
            }

            @Override
            public void onDisconnect() {
                log.info("设备 {}:{} 连接断开，取消定时任务", entity.getHost(), entity.getPort());

                ScheduledFuture<?> scheduledFuture = speedTaskMap.get(entity.getDeviceCode());
                if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                    scheduledFuture.cancel(false);
                    speedTaskMap.remove(entity.getDeviceCode());
                    temperatureTaskMap.remove(entity.getDeviceCode());
                    clientMap.remove(entity.getDeviceCode());
                    log.info("定时任务已取消");
                }
                byteBuf.release();

                redisUtils.setCacheObject("iot:device-status:speed:" + entity.getDeviceCode(), false);
            }

            @Override
            public void onException(Throwable cause) {
                log.error("设备 {}:{} 连接异常: {}", entity.getHost(), entity.getPort(), cause.getMessage());
                onDisconnect();

            }
        });

        // 连接客户端
        client.connect();
    }

    private void processCommonData(String deviceCode, short[] commonRegisters, long l, ByteBuf byteBuf) {
        try {
            short addr0 = commonRegisters[0];
            boolean bitStatus1 = ModbusUtil.getBitStatus(addr0, 1, true);

            redisUtils.setCacheObject("iot:device-status:site:heartBeat:" + deviceCode, true, 5, TimeUnit.SECONDS);
            redisUtils.setCacheObject("iot:device-status:site:status:" + deviceCode, bitStatus1);

        } catch (Exception e) {
            log.error("处理公共数据失败: {}", e.getMessage());
        }
    }

    @Override
    public void disconnect(String deviceCode) {
        ModbusTcpClient client = clientMap.get(deviceCode);
        if (client == null) {
            throw new BusinessException("设备不在线!");
        }
        client.disconnect();
    }

    private void processSpeedData(String unit, short[] shortData, long dataTime, ByteBuf byteBuf) {

        // 处理转速数据（例如：rGeneratorSpeed, rGeneratorSpeedDelay 等）
        float rGeneratorSpeed = getFloatFromRegisters(shortData[0] & 0xFFFF, shortData[1] & 0xFFFF, byteBuf);
        float rGeneratorSpeedDelay = getFloatFromRegisters(shortData[2] & 0xFFFF, shortData[3] & 0xFFFF, byteBuf);
        float rGeneratorSpeedFluctuateDelay = getFloatFromRegisters(shortData[4] & 0xFFFF, shortData[5] & 0xFFFF, byteBuf);
        float rGeneratorSpeedAverageDelay = getFloatFromRegisters(shortData[6] & 0xFFFF, shortData[7] & 0xFFFF, byteBuf);

        System.out.println("rGeneratorSpeed = " + rGeneratorSpeed);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("unit", unit);
        jsonObject.put("rGeneratorSpeed", rGeneratorSpeed);
        jsonObject.put("rGeneratorSpeedDelay", rGeneratorSpeedDelay);
        jsonObject.put("rGeneratorSpeedFluctuateDelay", rGeneratorSpeedFluctuateDelay);
        jsonObject.put("rGeneratorSpeedAverageDelay", rGeneratorSpeedAverageDelay);
        jsonObject.put("timestamp", dataTime);


        // 解析寄存器 18 的 bit 位
        int addr18 = shortData[8] & 0xFFFF;

        boolean bitStatus0 = ModbusUtil.getBitStatus(addr18, 0, true);
        boolean bitStatus1 = ModbusUtil.getBitStatus(addr18, 1, true);
        boolean bitStatus2 = ModbusUtil.getBitStatus(addr18, 2, true);
        boolean bitStatus3 = ModbusUtil.getBitStatus(addr18, 3, true);
        boolean bitStatus4 = ModbusUtil.getBitStatus(addr18, 4, true);
        boolean bitStatus5 = ModbusUtil.getBitStatus(addr18, 5, true);
        boolean bitStatus6 = ModbusUtil.getBitStatus(addr18, 6, true);

        jsonObject.put("bGeneratorSpeedFFTActive", bitStatus0);
        jsonObject.put("bGeneratorSpeedLimit1Active", bitStatus1);
        jsonObject.put("bGeneratorSpeedLimit2Active", bitStatus2);
        jsonObject.put("bGeneratorSpeedLimit3Active", bitStatus3);
        jsonObject.put("bGeneratorSpeedFluctuate1Active", bitStatus4);
        jsonObject.put("bGeneratorSpeedFluctuate2Active", bitStatus5);
        jsonObject.put("bGeneratorSpeedFluctuate3Active", bitStatus6);

        rabbitTemplate.convertAndSend(RabbitMqConfig.SPEED_DATA_EXCHANGE, RabbitMqConfig.SPEED_ROUTING_KEY, jsonObject.toJSONString());

    }

    private void processTemperatureData(String deviceCode, short[] temperatureLongTimeRegisters, short[] registers2, long dataTime) {

        executorService.submit(() -> {
            ByteBuf readBuf = Unpooled.buffer(256);

            JSONObject jsonObject = new JSONObject();

            int longTimeActiveTI1 = registers2[0] & 0xFFFF;
            int longTimeActiveTI2 = registers2[2] & 0xFFFF;

            int limit1ActiveTI1 = registers2[4] & 0xFFFF;
            int limit1ActiveTI2 = registers2[6] & 0xFFFF;

            int limit2ActiveTI1 = registers2[8] & 0xFFFF;
            int limit2ActiveTI2 = registers2[10] & 0xFFFF;

            int limit3ActiveTI1 = registers2[12] & 0xFFFF;
            int limit3ActiveTI2 = registers2[14] & 0xFFFF;


            float[] TI1Temperatures = new float[10];
            float[] TI2Temperatures = new float[10];

            boolean[] TI1AreaStatus = getTemperatureAreaStatus(longTimeActiveTI1, 10);
            boolean[] TI2AreaStatus = getTemperatureAreaStatus(longTimeActiveTI2, 10);

            boolean[] TI1AreaLimit1ActiveStatus = getTemperatureAreaStatus(limit1ActiveTI1, 10);
            boolean[] TI2AreaLimit1ActiveStatus = getTemperatureAreaStatus(limit1ActiveTI2, 10);

            boolean[] TI1AreaLimit2ActiveStatus = getTemperatureAreaStatus(limit2ActiveTI1, 10);
            boolean[] TI2AreaLimit2ActiveStatus = getTemperatureAreaStatus(limit2ActiveTI2, 10);

            boolean[] TI1AreaLimit3ActiveStatus = getTemperatureAreaStatus(limit3ActiveTI1, 10);
            boolean[] TI2AreaLimit3ActiveStatus = getTemperatureAreaStatus(limit3ActiveTI2, 10);


            for (int i = 0; i < AREA_COUNT; i++) {
                TI1Temperatures[i] = getTemperatureFromRegisters(temperatureLongTimeRegisters[i * 2], temperatureLongTimeRegisters[i * 2 + 1], readBuf);
                TI2Temperatures[i] = getTemperatureFromRegisters(temperatureLongTimeRegisters[(i + 10) * 2], temperatureLongTimeRegisters[(i + 10) * 2 + 1], readBuf);
            }

            for (int i = 0; i < AREA_COUNT; i++) {
                jsonObject.put("TI1Area" + (i + 1) + "TemperatureLongTime", TI1Temperatures[i]);
                jsonObject.put("TI2Area" + (i + 1) + "TemperatureLongTime", TI2Temperatures[i]);
            }

            for (int i = 0; i < AREA_COUNT; i++) {
                jsonObject.put("TI1Area" + (i + 1) + "TemperatureLongTimeActive", TI1AreaStatus[i]);
                jsonObject.put("TI2Area" + (i + 1) + "TemperatureLongTimeActive", TI2AreaStatus[i]);

                jsonObject.put("TI1Area" + (i + 1) + "Temperature" + "Limit1Active", TI1AreaLimit1ActiveStatus[i]);
                jsonObject.put("TI2Area" + (i + 1) + "Temperature" + "Limit1Active", TI2AreaLimit1ActiveStatus[i]);

                jsonObject.put("TI1Area" + (i + 1) + "Temperature" + "Limit2Active", TI1AreaLimit2ActiveStatus[i]);
                jsonObject.put("TI2Area" + (i + 1) + "Temperature" + "Limit2Active", TI2AreaLimit2ActiveStatus[i]);

                jsonObject.put("TI1Area" + (i + 1) + "Temperature" + "Limit3Active", TI1AreaLimit3ActiveStatus[i]);
                jsonObject.put("TI2Area" + (i + 1) + "Temperature" + "Limit3Active", TI2AreaLimit3ActiveStatus[i]);
            }
            jsonObject.put("dataTime", dataTime);
            jsonObject.put("siteId", deviceCode);
            readBuf.release();

            rabbitTemplate.convertAndSend(RabbitMqConfig.TI_DATA_EXCHANGE, RabbitMqConfig.TI_ROUTING_KEY, jsonObject.toJSONString());
        });
    }

    private float getFloatFromRegisters(int high, int low, ByteBuf byteBuf) {
        byteBuf.clear();
        byteBuf.writeShort((short) high);
        byteBuf.writeShort((short) low);

        return byteBuf.readFloat();  // 转换为 float 类型
    }

    public float getTemperatureFromRegisters(int register1, int register2, ByteBuf readBuf) {
        return getFloatFromRegisters(register1 & 0xFFFF, register2 & 0xFFFF, readBuf);
    }

    public static boolean[] getTemperatureAreaStatus(int longTimeActive, int areaCount) {
        boolean[] areaStatus = new boolean[areaCount];
        for (int i = 0; i < areaCount; i++) {
            areaStatus[i] = ModbusUtil.getBitStatus(longTimeActive, i);
        }
        return areaStatus;
    }


    private List<ModbusClientEntity> getClinetList() {
        List<ModbusClientEntity> clientList = new ArrayList<>();
        List<TSpeedInfo> list = speedInfoService.list();
        for (TSpeedInfo speedInfo : list) {
            ModbusClientEntity clientEntity = new ModbusClientEntity(speedInfo.getIp(), speedInfo.getPort(), speedInfo.getSlaveId(), speedInfo.getSiteId() + "");
            clientList.add(clientEntity);
        }
        return clientList;
    }
}
