package com.tc.modules.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OverviewVO", description = "首页数据")
public class OverviewVO {

    @ApiModelProperty(value = "风机ID")
    private Integer siteId;

    @ApiModelProperty(value = "风机名称")
    private String siteName;

    @ApiModelProperty(value = "温度状态 0-正常，1-闪烁，2-告警")
    private Integer temperatureStatus;

    @ApiModelProperty(value = "温度状态 0-正常，1-闪烁，2-告警")
    private Integer speedStatus;

    @ApiModelProperty(value = "视频状态 0-正常，1-闪烁，2-告警")
    private Integer videoStatus;

    @ApiModelProperty(value = "音频状态 0-正常，1-闪烁，2-告警")
    private Integer audioStatus;

}
