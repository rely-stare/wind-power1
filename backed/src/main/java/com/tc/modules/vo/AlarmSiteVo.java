package com.tc.modules.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 风电机组告警数量趋势图
 * @author sunchao
 */
@Data
public class AlarmSiteVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("数量")
    private String sl;

    @ApiModelProperty("风电机组")
    private String siteName;

    @ApiModelProperty("告警时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String alarmTime;


}