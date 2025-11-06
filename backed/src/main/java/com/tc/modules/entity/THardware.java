package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_hardware")
@ApiModel(value = "THardware", description = "")
public class THardware {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("硬件类型")
    @TableField("hardware_type")
    private Integer hardwareType;

    @ApiModelProperty("硬件编码")
    @TableField("hardware_code")
    private String hardwareCode;

    @ApiModelProperty("监控位置")
    @TableField("monitor_location")
    private String monitorLocation;

    @ApiModelProperty("监控类型")
    @TableField("monitor_type")
    private Integer monitorType;

    @ApiModelProperty("风机ID")
    @TableField("site_id")
    private Integer siteId;

    @ApiModelProperty("是否显示")
    @TableField("is_show")
    private Integer isShow;

    @ApiModelProperty("创建人")
    @TableField("create_by")
    private String createBy;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField(exist = false)
    private String label;
}
