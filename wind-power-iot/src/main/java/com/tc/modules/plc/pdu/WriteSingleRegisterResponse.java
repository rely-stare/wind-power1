package com.tc.modules.plc.pdu;

import cn.hutool.core.util.HexUtil;
import com.tc.modules.plc.consts.FunctionCode;

import java.nio.ByteBuffer;

public class WriteSingleRegisterResponse {
    /**
     * 寄存器地址
     */
    protected int address;
    /**
     * 数值
     */
    protected int value;


    public WriteSingleRegisterResponse(int address, int value) {
        this.address = address;
        this.value = value;
    }

    public static WriteSingleRegisterResponse decode(ByteBuffer buffer) {
        int functionCode = buffer.get();
        assert functionCode == FunctionCode.WRITE_SINGLE_REGISTER.getCode();

        int address = buffer.getShort() & 0xFFFF;
        int value = buffer.getShort() & 0xFFFF;

        return new WriteSingleRegisterResponse(address, value);
    }

    public static WriteSingleRegisterResponse parseResponse(byte[] responseBytes){
        ByteBuffer buffer = ByteBuffer.wrap(responseBytes);

//        System.out.println("receive = " + HexUtil.encodeHexStr(buffer.array()));


        // 解析 Modbus 响应的字段
        int transactionId = buffer.getShort();  // 事务 ID
        int protocolId = buffer.getShort();     // 协议标识符，Modbus TCP 固定为 0x0000
        int length = buffer.getShort();         // 长度字段
        int slaveId = buffer.get();         // 从站 ID
        int functionCode = buffer.get();    // 功能码

        // 校验功能码是否正确
        if (functionCode != FunctionCode.WRITE_SINGLE_REGISTER.getCode()) {
            throw new RuntimeException("Invalid function code in response.");
        }


        int address = buffer.getShort() & 0xFFFF;
        int value = buffer.getShort() & 0xFFFF;

        return new WriteSingleRegisterResponse(address, value);
    }
}
