package com.tc.modules.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tc.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 转速数据vo
 * @author sunchao
 */
@Data
public class SpeedDataVo implements Serializable {

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

    @ApiModelProperty("转速")
    @Excel(name = "转速")
    private String speed;

    @ApiModelProperty("时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Excel(name = "时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date time;


}