package com.tc.modules.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@Data
public class FFTActiveVo {

    private Integer id;

    @ApiModelProperty(value = "风机ID")
    private Integer siteId;

    @ApiModelProperty(value = "风机名称")
    private String siteName;

    @ApiModelProperty(value = "监控点")
    private String monitorLocation;

    @ApiModelProperty(value = "监控信息")
    private String monitorInfo;

    @ApiModelProperty(value = "触发时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date activeTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date time;

}
