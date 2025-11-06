package com.tc.modules.plc.pdu;

import io.netty.buffer.ByteBuf;

public class WriteMultipleRegistersRequest {

    private final int startAddress;
    private final int[] values;

    public WriteMultipleRegistersRequest(int startAddress, int[] values) {
        this.startAddress = startAddress;
        this.values = values;
    }

    public static void encode(WriteMultipleRegistersRequest request, ByteBuf out) {
        out.writeByte(0x10); // Function code
        out.writeShort(request.startAddress);
        out.writeShort(request.values.length);
        out.writeByte(request.values.length * 2);
        for (int value : request.values) {
            out.writeShort(value);
        }
    }
}
