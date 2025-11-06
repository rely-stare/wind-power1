package com.tc.common.enums;

import lombok.Getter;

@Getter
public enum AlarmType {

    SPEED_ALARM(1,"转速告警"),

    STABILITY_ALARM(2,"稳定性告警"),

    OSCILLATION_ALARM(3,"震荡告警"),

    FREQUENCY_ALARM(4,"频谱告警"),

    TI_ALARM(5,"超温告警"),

    VIDEO_ALARM(6,"视频告警"),

    AUDIO_ALARM(7,"音频告警");

    private final int code;
    private final String message;

    AlarmType(int code, String message) {
        this.message = message;
        this.code = code;
    }
}
