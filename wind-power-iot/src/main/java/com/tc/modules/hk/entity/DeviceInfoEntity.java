package com.tc.modules.hk.entity;


import lombok.Data;

@Data
public class DeviceInfoEntity {

    private int cameraId;

    private int hardwareId;

    private String hardwareCode;

    private String ip;

    private int port;

    private String username;

    private String password;

}
