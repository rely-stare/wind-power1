package com.tc.modules.plc.service;

import com.tc.modules.plc.entity.ModbusClientEntity;
import com.tc.modules.plc.pdu.ReadHoldingRegistersResponse;
import com.tc.modules.plc.pdu.WriteMultipleRegistersResponse;
import com.tc.modules.plc.pdu.WriteSingleRegisterResponse;

import java.util.List;

public interface ModbusService {

    /**
     * 读取保持寄存器的值。
     *
     * @param deviceCode 设备编码
     * @param address    寄存器地址
     * @param bitIndex      读取的位索引
     * @return 返回写入操作的结果响应
     * @throws Exception 如果写入过程中发生异常
     */
    boolean ReadSingRegisterBit(String deviceCode, int address, int bitIndex) throws Exception;


    /**
     * 读取保持寄存器的值 float。
     *
     * @param deviceCode 设备编码
     * @param address    寄存器地址
     * @return 返回写入操作的结果响应
     * @throws Exception 如果写入过程中发生异常
     */
    float ReadSingRegisterReal(String deviceCode, int address) throws Exception;

    /**
     * 读取保持寄存器的值。
     *
     * @param deviceCode 设备编码
     * @param address    寄存器地址
     * @param count      读取数量
     * @return 返回写入操作的结果响应
     * @throws Exception 如果写入过程中发生异常
     */
    ReadHoldingRegistersResponse ReadHoldingRegisters(String deviceCode, int address, int count) throws Exception;

    /**
     * 写入单个寄存器的值。
     *
     * @param deviceCode 设备编码
     * @param address    寄存器地址
     * @param value      要写入的整数值
     * @return 返回写入操作的结果响应
     * @throws Exception 如果写入过程中发生异常
     */
    WriteSingleRegisterResponse writeSingleRegister(String deviceCode, int address, int value) throws Exception;

    /**
     * 写入单个寄存器中的指定位。
     *
     * @param deviceCode 设备编码
     * @param address    寄存器地址
     * @param value      要写入的布尔值
     * @param bitIndex   要写入的位索引
     * @return 返回写入操作的结果响应
     * @throws Exception 如果写入过程中发生异常
     */
    WriteSingleRegisterResponse writeSingleRegisterBit(String deviceCode, int address, boolean value, int bitIndex) throws Exception;

    /**
     * 写入单个寄存器的浮点数
     *
     * @param deviceCode 设备编码
     * @param address    寄存器地址
     * @param value      要写入的浮点数值
     * @return 返回包含多个写入操作的结果响应列表
     * @throws Exception 如果写入过程中发生异常
     */
    WriteMultipleRegistersResponse writeSingleRegisterReal(String deviceCode, int address, float value) throws Exception;

    /**
     * 初始化服务，通常在启动时调用。
     */
    void init();

    /**
     * 连接到Modbus客户端。
     *
     * @param entity 包含连接信息的实体对象
     */
    void connect(ModbusClientEntity entity) throws Exception;

    /**
     * 断开连接
     *
     * @param deviceCode 设备编码
     */
    void disconnect(String deviceCode);
}
