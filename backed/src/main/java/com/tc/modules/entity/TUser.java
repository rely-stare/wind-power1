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
 * 用户表
 * </p>
 *
 * @author Jz
 * @since 2023-12-11
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_user")
@ApiModel(value = "TUser对象", description = "用户表")
public class TUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户姓名")
    @TableField("full_Name")
    private String fullName;

    @ApiModelProperty("用户名（登录名）")
    @TableField("user_name")
    private String userName;

    @ApiModelProperty("密码")
    @TableField("password")
    private String password;

    @ApiModelProperty("密码失效时间")
    @TableField("password_invalid_time")
    private Date passwordInvalidTime;

    @ApiModelProperty("电话号码")
    @TableField("telephone")
    private String telephone;

    @ApiModelProperty("创建者id")
    @TableField("create_by")
    private Integer createBy;

    @ApiModelProperty("用户创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty("组织机构id")
    @TableField("org_id")
    private Integer orgId;

    @ApiModelProperty("用户角色id")
    @TableField("role_id")
    private Integer roleId;

    @ApiModelProperty("用户状态：1-可用；0-禁用")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("是否删除：0-正常；1-已删除")
    @TableField("is_delete")
    private Integer isDelete;

    @ApiModelProperty("登录ip地址")
    @TableField("ip_address")
    private String ipAddress;

    @ApiModelProperty("最后登录时间")
    @TableField("last_login_time")
    private Date lastLoginTime;


}
