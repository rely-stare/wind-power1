package com.tc.modules.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_nvr_info")
@ApiModel(value = "THardwareTemplate", description = "")
public class TNvrInfo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("nvr名称")
    private String nvrName;

    @ApiModelProperty("nvrIp")
    private String nvrIp;

    @ApiModelProperty("nvr端口")
    private String nvrPort;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("创建人")
    private Integer createBy;

    @ApiModelProperty("创建时间")
    private Date createTime;

}
