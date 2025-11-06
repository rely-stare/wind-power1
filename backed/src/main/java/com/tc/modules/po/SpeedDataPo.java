package com.tc.modules.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 转速数据po
 * @author sunchao
 */
@Data
public class SpeedDataPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("风电场ID")
    private Integer id;

    @ApiModelProperty("风电机组")
    private String siteName;

    @ApiModelProperty("时间")
    private String time;


}