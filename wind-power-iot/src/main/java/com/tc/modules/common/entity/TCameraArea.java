package com.tc.modules.common.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_camera_area")
public class TCameraArea {

    private Integer id;

    private Integer cameraId;

    private Integer ruleId;

    private String areaName;

    private String ModbusField;

}
