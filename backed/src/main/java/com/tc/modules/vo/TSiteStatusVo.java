package com.tc.modules.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TSiteStatusVo {

    @ApiModelProperty(value = "风机ID")
    private Integer siteId;

    @ApiModelProperty(value = "风机状态 0-维护，1-正常")
    private Integer status;

}
