package com.tc.modules.plc.consts;

public enum DataType {

    BIT(1),
    INT(2),
    REAL(3);

    private final int code;

    DataType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
