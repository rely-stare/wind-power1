package com.tc.modules.common.entity;

import lombok.Data;

@Data
public class TModbusProtocol {

    private Integer id;

    private String mode;

    private String field;

    private String name;

    private String type;

    private Integer address;

    private Integer bitIndex;

    private String modules;

    private String remark;
}
