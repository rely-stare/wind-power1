package com.tc.modules.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 声音数据po
 * @author sunchao
 */
@Data
public class VoiceDataPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("硬件ID")
    private Integer hardwareId;


    @ApiModelProperty("开始时间")
    private String startTime ;

    @ApiModelProperty("结束时间")
    private String endTime ;


}