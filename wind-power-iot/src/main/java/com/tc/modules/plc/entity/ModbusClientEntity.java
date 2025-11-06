package com.tc.modules.plc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModbusClientEntity {

    /**
     * 设备ip地址
     */
    private String host;

    /**
     * 设备端口
     */
    private int port;

    /**
     * 从机地址
     */
    private int slaveId;

    /**
     * 设备编码
     */
    private String deviceCode;
}
