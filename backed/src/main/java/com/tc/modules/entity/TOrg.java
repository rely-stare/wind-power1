package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 组织架构+电力设备表
 * </p>
 *
 * @author Jz
 * @since 2023-12-11
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_org")
@ApiModel(value = "TOrg对象", description = "组织架构+电力设备表")
public class TOrg implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("父级节点id")
    @TableField("parent_id")
    private Integer parentId;

    @ApiModelProperty("组织名")
    @TableField("org_name")
    private String orgName;

    @ApiModelProperty("组织编码")
    @TableField("org_code")
    private String orgCode;

    @ApiModelProperty("组织类型（1-风电场、2-风电机组 3-风机区域 4-主设备 5-监控点）")
    @TableField("org_type")
    private Integer orgType;

    @ApiModelProperty("创建者id")
    @TableField("create_by")
    private Integer createBy;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty("0-正常，1-已删除")
    @TableField("is_delete")
    private Integer isDelete;


}
