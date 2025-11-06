package com.tc.modules.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tc.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author sunchao
 */
@Data
public class InfraredDataVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("监控点ID")
    private Integer orgId;

    private Integer parentId;

    @ApiModelProperty("风电机组")
    @Excel(name = "风电机组")
    private String siteName;

    @ApiModelProperty("区域")
    @Excel(name = "区域")
    private String areaName;

    @ApiModelProperty("主设备")
    @Excel(name = "主设备")
    private String mainDevice;

    @ApiModelProperty("监控点")
    @Excel(name = "监控点")
    private String pointName;

    @ApiModelProperty("最高温度")
    @Excel(name = "最高温度")
    private String maxTemperature;

    @ApiModelProperty("最低温度")
    @Excel(name = "最低温度")
    private String minTemperature;

    @ApiModelProperty("平均温度")
    @Excel(name = "平均温度")
    private String avgTemperature;

    @ApiModelProperty("采集信息")
    @Excel(name = "采集信息")
    private String resource;

    @Excel(name = "时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

}