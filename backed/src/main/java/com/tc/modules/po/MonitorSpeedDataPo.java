package com.tc.modules.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 转速监控po
 * @author sunchao
 */
@Data
public class MonitorSpeedDataPo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    @ApiModelProperty("风电场ID")
    private Integer orgId;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("起始频率")
    private Long startFrequenc;

    @ApiModelProperty("截至频率")
    private Long endFrequenc;

    private Integer limit;

}