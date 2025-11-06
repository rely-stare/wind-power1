package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("t_speed_fft_active")
@ApiModel(value = "TSpeedFFTActive", description = "转速数据表")
public class TSpeedFFTActive {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("风机ID")
    private Integer siteId;

    @ApiModelProperty("风机ID")
    private Integer alarmId;

    @ApiModelProperty("分析结果")
    @JsonIgnore
    private String result;

    @ApiModelProperty("触发时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date activeTime;
}
