package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 视频主机摄像头
 */
@Data
@TableName("t_camera_info")
@ApiModel(value = "TCameraInfo", description = "")
public class TCameraInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("摄像机名称")
    private String cameraName;

    @ApiModelProperty("ip")
    private String ip;

    @ApiModelProperty("端口")
    private String port;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("通道号")
    private Integer channelId;

    @ApiModelProperty("nvrId")
    private Integer nvrId;

    @ApiModelProperty("nvr通道号码")
    private Integer nvrChannelId;

    @ApiModelProperty("是否删除 0-否 1-是")
    private Integer isDelete;

    @ApiModelProperty("硬件id")
    private Integer hardwareId;

    @ApiModelProperty("创建人")
    private Integer createBy;

    @ApiModelProperty("图片")
    private String baseImage;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新人")
    private Integer updateBy;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @NotNull(message = "页码为空")
    @TableField(exist = false)
    private Integer page;

    @NotNull(message = "条数为空")
    @Min(value = 1, message = "最少查询1条")
    @Max(value = 100, message = "最多查询100条")
    @TableField(exist = false)
    private Integer size;

    @TableField(exist = false)
    private String siteName;

    @TableField(exist = false)
    private String siteId;

}