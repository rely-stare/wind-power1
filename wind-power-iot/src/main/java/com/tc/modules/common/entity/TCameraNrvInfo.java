package com.tc.modules.common.entity;


import lombok.Data;

@Data
public class TCameraNrvInfo {

    private Integer nvrId;

    private String nvrIp;

    private Short nvrPort;

    private String nvrUser;

    private String nvrPsw;

    private Integer cameraId;

    private Integer hardwareId;

    private Integer nvrChannelId;

}
