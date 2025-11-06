package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 告警记录表
 * @author sunchao
 * @TableName t_alarm
 */
@Data
@ApiModel(value = "TAlarm", description = "")
public class TAlarm implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "风机ID")
    private Integer siteId;

    @ApiModelProperty(value = "设备ID")
    private String hardwareId;

    @ApiModelProperty(value = "告警类型 1-转速，2-稳定性，3-震荡，4-频谱，5-热成像，6-视频，7-音频")
    private Integer alarmType;

    @ApiModelProperty(value = "告警级别")
    private Integer alarmLevel;

    @ApiModelProperty(value = "告警内容")
    private String alarmContent;

    @ApiModelProperty(value = "监控点")
    private String monitorLocation;

    @ApiModelProperty(value = "是否已处理")
    private Integer isCheck;

    @ApiModelProperty(value = "处理人")
    private String checkUser;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "告警时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date alarmTime;


    @TableField(exist = false)
    private String siteName;

    @TableField(exist = false)
    private Integer monitorType;

    @TableField(exist = false)
    private Integer hardwareType;


}