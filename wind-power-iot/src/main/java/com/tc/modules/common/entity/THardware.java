package com.tc.modules.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_hardware")
public class THardware {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("hardware_type")
    private Integer hardwareType;

    @TableField("hardware_code")
    private String hardwareCode;

    @TableField("monitor_location")
    private String monitorLocation;

    @TableField("site_id")
    private Integer siteId;

    @TableField("is_show")
    private Integer isShow;

    @TableField("create_by")
    private String createBy;

    @TableField("create_time")
    private Date createTime;

    @TableField(exist = false)
    private String label;
}
