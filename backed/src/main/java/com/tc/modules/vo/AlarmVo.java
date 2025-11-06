package com.tc.modules.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tc.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 告警记录vo
 * @author sunchao
 */
@Data
public class AlarmVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer parentId;

    @ApiModelProperty("风电场")
    private String factionName;

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

    @ApiModelProperty("监控点ID")
    private Integer orgId;

    @ApiModelProperty("检测结果")
    @Excel(name = "检测结果")
    private String checkResult;

    @ApiModelProperty("检测类别")
    @Excel(name = "检测类别")
    private Integer alarmType;

    @ApiModelProperty("采集信息")
    @Excel(name = "采集信息")
    private String resource;

    @ApiModelProperty("告警等级")
    @Excel(name = "告警等级")
    private String alarmLevel;

    @ApiModelProperty("告警规则")
    @Excel(name = "告警规则")
    private String alarmRule;

    @ApiModelProperty("检测来源")
    @Excel(name = "检测来源")
    private String alarmSource;

    @Excel(name = "时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("告警时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String alarmTime;

    @ApiModelProperty("处理结果(0-未处理 1-未处理 2-误报)")
    @Excel(name = "处理结果")
    private Integer handleResult;

    @ApiModelProperty("备注")
    @Excel(name = "备注")
    private String remark;

}