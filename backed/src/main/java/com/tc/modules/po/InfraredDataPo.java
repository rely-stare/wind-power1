package com.tc.modules.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 红外数据po
 * @author sunchao
 */
@Data
public class InfraredDataPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("风电场ID")
    private Integer id;

    @ApiModelProperty("监控点ID")
    private Integer orgId;

    @ApiModelProperty("风电机组")
    private String siteName;

    @ApiModelProperty("时间")
    private String time;


}