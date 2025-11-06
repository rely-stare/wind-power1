package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel(value = "TSpeedInfo", description = "")
public class TSpeedInfo implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("转速设备名称")
    private String speedName;

    @ApiModelProperty("ip")
    private String ip;

    @ApiModelProperty("port")
    private Integer port;

    @ApiModelProperty("从机地址")
    private Integer slaveId;

    @ApiModelProperty("风机ID")
    private Integer siteId;

    @ApiModelProperty("是否删除 0-否 1-是")
    @TableLogic
    private Integer isDelete;

    @ApiModelProperty("创建人")
    private Integer createBy;

    @ApiModelProperty("更新人")
    private Integer updateBy;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;


}
