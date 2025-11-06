package com.tc.common.enums;

import lombok.Getter;

@Getter
public enum MonitorType {
    SPEED_OVERLIMIT(1, "转速-超限"),
    SPEED_STABILITY(2, "转速-稳定性"),
    SPEED_OSCILLATION(3, "转速-震荡"),
    SPEED_SPECTRUM(4, "转速-频谱"),
    TI_LEFT(5, "热成像-机舱左侧"),
    TI_RIGHT(6, "热成像-机舱右侧"),
    VIDEO_LEFT(7, "视频-机舱左侧"),
    VIDEO_RIGHT(8, "视频-机舱右侧"),
    VIDEO_TOWER_BASE(9, "视频-塔基"),
    AUDIO_FRONT(10, "机舱前"),
    AUDIO_REAR(11, "机舱后"),
    AUDIO_YAW_PLATFORM(12, "偏航平台"),
    AUDIO_GEARBOX(13, "齿轮箱");

    private final int type;
    private final String monitorLocation;

    MonitorType(int type, String monitorLocation) {
        this.type = type;
        this.monitorLocation = monitorLocation;
    }


}
