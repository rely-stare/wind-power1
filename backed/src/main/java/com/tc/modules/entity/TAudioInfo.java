package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "TAudioInfo", description = "")
public class TAudioInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "硬件ID")
    private Integer hardwareId;

    @ApiModelProperty(value = "设备编号")
    private String deviceNumber;

    @ApiModelProperty(value = "检测位置")
    private String detectPosition;

    @ApiModelProperty(value = "监控位置")
    @TableField(exist = false)
    private String monitorLocation;

    @TableField(exist = false)
    private String siteName;

    @TableField(exist = false)
    private Integer siteId;
}
