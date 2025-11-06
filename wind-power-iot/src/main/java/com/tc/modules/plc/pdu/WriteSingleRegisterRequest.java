package com.tc.modules.plc.pdu;

import com.tc.modules.plc.consts.FunctionCode;
import io.netty.buffer.ByteBuf;

public class WriteSingleRegisterRequest implements ModbusPdu {
    /**
     * 寄存器地址
     */
    protected int address;
    /**
     * 数值
     */
    protected int value;


    @Override
    public int getFunctionCode() {
        return FunctionCode.WRITE_SINGLE_REGISTER.getCode();
    }

    public WriteSingleRegisterRequest(int address, int value) {
        this.address = address;
        this.value = value;
    }

    public static void encode(WriteSingleRegisterRequest request, ByteBuf byteBuf) {
        byteBuf.writeByte(request.getFunctionCode());
        byteBuf.writeShort(request.address);
        byteBuf.writeShort(request.value);
    }
}
