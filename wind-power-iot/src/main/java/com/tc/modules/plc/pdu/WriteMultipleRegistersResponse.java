package com.tc.modules.plc.pdu;

public class WriteMultipleRegistersResponse {
    private final int startAddress;
    private final int quantity;

    public WriteMultipleRegistersResponse(int startAddress, int quantity) {
        this.startAddress = startAddress;
        this.quantity = quantity;
    }

    public static WriteMultipleRegistersResponse parseResponse(byte[] data) {
        int startAddr = ((data[8] & 0xFF) << 8) | (data[9] & 0xFF);
        int count = ((data[10] & 0xFF) << 8) | (data[11] & 0xFF);
        return new WriteMultipleRegistersResponse(startAddr, count);
    }
}

