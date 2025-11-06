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
 * 角色表
 * </p>
 *
 * @author Jz
 * @since 2023-12-11
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_role")
@ApiModel(value = "TRole对象", description = "角色表")
public class TRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("角色名")
    @TableField("role_name")
    private String roleName;

    @ApiModelProperty("角色类型 0-自定义 1-系统角色（系统角色不可编辑、删除）")
    @TableField("role_type")
    private Integer roleType;

    @ApiModelProperty("任务控制权限（1-具备 0-不具备）")
    @TableField("task_control")
    private Integer taskControl;

    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty("创建者用户id，系统角色的创建者id为0")
    @TableField("create_by")
    private Integer createBy;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty("0-正常，1-已删除")
    @TableField("is_delete")
    private Integer isDelete;


}
