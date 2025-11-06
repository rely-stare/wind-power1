package com.tc.common.enums;

import lombok.Getter;

@Getter
public enum HardwareType {

    SPEED(1,"转速"),
    TI(2,"热成像"),
    VIDEO(3,"视频"),
    AUDIO(4,"音频"),
    ;

    private final int code;
    private final String message;

    HardwareType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static HardwareType getHardwareType(int code) {
        for (HardwareType type : HardwareType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }
}
