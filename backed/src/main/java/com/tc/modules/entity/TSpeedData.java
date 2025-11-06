package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_speed_data")
@ApiModel(value = "TSpeedData", description = "转速数据表")
public class TSpeedData {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("风机ID")
    private Integer siteId;

    @ApiModelProperty("转速")
    private Double speed;

    @ApiModelProperty("转速延迟")
    private Double speedDelay;

    @ApiModelProperty("转速波动延迟")
    private Double speedFluctuateDelay;

    @ApiModelProperty("转速均值延迟")
    private Double speedAverageDelay;

    @ApiModelProperty("创建时间")
    private Date createTime;
}
