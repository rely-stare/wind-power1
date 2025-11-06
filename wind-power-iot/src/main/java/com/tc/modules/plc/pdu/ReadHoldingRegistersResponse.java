package com.tc.modules.plc.pdu;

import cn.hutool.core.util.HexUtil;
import com.tc.modules.plc.consts.FunctionCode;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class ReadHoldingRegistersResponse implements ModbusPdu {


    private byte[] data;

    private short[] registers;


    public ReadHoldingRegistersResponse(byte[] data, short[] registers) {
        this.data = data;
        this.registers = registers;
    }

    @Override
    public int getFunctionCode() {
        return FunctionCode.READ_HOLDING_REGISTERS.getCode();
    }

    public static ReadHoldingRegistersResponse parseResponse(byte[] responseBytes) {

        ByteBuffer buffer = ByteBuffer.wrap(responseBytes);

//        System.out.println("receive = " + HexUtil.encodeHexStr(buffer.array()));

        // 解析 Modbus 响应的字段
        int transactionId = buffer.getShort();  // 事务 ID
        int protocolId = buffer.getShort();     // 协议标识符，Modbus TCP 固定为 0x0000
        int length = buffer.getShort();         // 长度字段
        int slaveId = buffer.get();         // 从站 ID
        int functionCode = buffer.get();    // 功能码
        int byteCount = buffer.get();       // 字节数

        // 校验功能码是否正确
        if (functionCode != FunctionCode.READ_HOLDING_REGISTERS.getCode()) {
            throw new RuntimeException("Invalid function code in response.");
        }

        // 读取返回的数据
        byte[] data = new byte[byteCount];
        buffer.get(data,0,data.length);

        short[] registers = new short[byteCount / 2];
        for (int i = 0; i < byteCount / 2; i++) {
            // 将每 2 个字节转换为一个 16 位的短整数，遵循大端字节序
            registers[i] = (short) ((data[i * 2] << 8) | (data[i * 2 + 1] & 0xFF));
        }

        return new ReadHoldingRegistersResponse(data, registers);
    }
}
