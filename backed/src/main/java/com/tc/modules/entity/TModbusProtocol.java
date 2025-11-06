package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TModbusProtocol {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("模式")
    private String mode;

    @ApiModelProperty("字段")
    private String field;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("地址")
    private Integer address;

    @ApiModelProperty("位")
    private Integer bitIndex;

    @ApiModelProperty("模块")
    private String modules;

    @ApiModelProperty("备注")
    private String remark;

}
