package com.tc.modules.plc.pdu;


import com.tc.modules.plc.consts.FunctionCode;
import io.netty.buffer.ByteBuf;
import lombok.Data;


@Data
public class ReadHoldingRegistersRequest implements ModbusPdu {
    /**
     * 寄存器地址
     */
    protected int address;
    /**
     * 数量
     */
    protected int quantity;

    @Override
    public int getFunctionCode() {
        return FunctionCode.READ_HOLDING_REGISTERS.getCode();
    }

    public ReadHoldingRegistersRequest(int address, int quantity)  {
        this.address = address;
        this.quantity = quantity;
    }

    public static void encode(ReadHoldingRegistersRequest request, ByteBuf byteBuf) {
        byteBuf.writeByte((byte) request.getFunctionCode());
        byteBuf.writeShort((short) request.address);
        byteBuf.writeShort((short) request.quantity);
    }

}
