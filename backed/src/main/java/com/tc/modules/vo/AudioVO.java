package com.tc.modules.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class AudioVO {

    @ApiModelProperty(value = "文件ID")
    private String fileID;

    @ApiModelProperty(value = "风机ID")
    private Integer siteId;

    @ApiModelProperty(value = "风机名称")
    private String siteName;

    @ApiModelProperty(value = "监控点")
    private String monitorLocation;

    @ApiModelProperty(value = "监控信息")
    private String monitorInfo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date time;

}
