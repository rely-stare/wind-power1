package com.tc.modules.hk.enums;

public enum NET_SDK_CALLBACK_TYPE {

    NET_SDK_CALLBACK_TYPE_STATUS(0),
    NET_SDK_CALLBACK_TYPE_PROGRESS(1),
    NET_SDK_CALLBACK_TYPE_DATA(2);

    private final int value;

    NET_SDK_CALLBACK_TYPE(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static NET_SDK_CALLBACK_TYPE fromValue(int value) {
        for (NET_SDK_CALLBACK_TYPE type : NET_SDK_CALLBACK_TYPE.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
}
