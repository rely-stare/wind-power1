package com.tc.modules.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_long_time_temperature")
@ApiModel(value = "TLongTimeTemperature", description = "")
public class TLongTimeTemperature {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String areaName;

    private Integer hardwareId;

    private Integer cameraId;

    private String temperature;

    private Date dateTime;

}
