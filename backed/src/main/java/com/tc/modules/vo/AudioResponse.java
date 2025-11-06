package com.tc.modules.vo;

import lombok.Data;

import java.util.Date;

@Data
public class AudioResponse {
    private double duration;
    private DetectStation detectStation;
    private DetectPoint detectPoint;
    private String aiResult;
    private String detectStationID;
    private Date startTime;
    private String id;
    private Date endTime;
    private Device device;
    private String fileID;
}

@Data
class DetectStation {
    private String number;
    private String name;
    private String id;
}

@Data
class DetectPoint {
    private String number;
    private String name;
    private int channel;
    private String id;
}

@Data
class Device {
    private String id;
    private String deviceName;
    private String deviceNumber;
}