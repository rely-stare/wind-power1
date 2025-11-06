package com.tc.modules.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 告警记录po
 * @author sunchao
 */
@Data
public class AlarmPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("设备编号")
    private String deviceCode;

    @ApiModelProperty("告警类型")
    private Integer alarmType;

    @ApiModelProperty("告警等级")
    private Integer alarmLevel;

    @ApiModelProperty("检测来源")
    private String alarmSource;

    @ApiModelProperty("告警时间")
    private String alarmTime;

}