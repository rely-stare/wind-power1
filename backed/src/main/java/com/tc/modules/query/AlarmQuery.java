package com.tc.modules.query;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AlarmQuery", description = "")
public class AlarmQuery {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "风机ID")
    private Integer siteId;

    @ApiModelProperty(value = "组织ID")
    private Integer orgId;

    @ApiModelProperty(value = "设备ID")
    private Integer hardwareId;

    @ApiModelProperty(value = "告警级类别")
    private Integer alarmType;

    @ApiModelProperty(value = "是否已处理")
    private Integer isCheck;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

}
