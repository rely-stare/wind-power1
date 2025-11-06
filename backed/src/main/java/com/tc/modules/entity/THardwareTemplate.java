package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_hardware_template")
@ApiModel(value = "THardwareTemplate", description = "")
public class THardwareTemplate {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("硬件类型")
    @TableField("hardware_type")
    private Integer hardwareType;

    @ApiModelProperty("监控位置")
    @TableField("monitor_location")
    private String monitorLocation;

    @ApiModelProperty("监控类型")
    @TableField("monitor_type")
    private Integer monitorType;

    @ApiModelProperty("版本")
    @TableField("version_id")
    private Integer versionId;

    @TableField(exist = false)
    private String label;

}
