package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author Jz
 * @since 2023-11-24
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_menu")
@ApiModel(value = "TMenu对象", description = "")
public class TMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("菜单id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("父级菜单id")
    @TableField("parent_id")
    private Integer parentId;

    @ApiModelProperty("菜单类型 1-目录 2-菜单 3-按钮")
    @TableField("menu_type")
    private Integer menuType;

    @ApiModelProperty("菜单显示名")
    @TableField("title")
    private String title;

    @ApiModelProperty("图标名")
    @TableField("icon_pic")
    private String iconPic;

    @ApiModelProperty("菜单名")
    @TableField("name")
    private String name;

    @ApiModelProperty("菜单路径")
    @TableField("path")
    private String path;

    @ApiModelProperty("排序")
    @TableField("sort")
    private Integer sort;

    @ApiModelProperty("是否展示 1-显示 0-隐藏")
    @TableField("is_show")
    private Integer isShow;

    @ApiModelProperty("重定向菜单路径")
    @TableField("redirect")
    private String redirect;

    @ApiModelProperty("菜单状态 1-可用 0-不可用")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("是否为外链 0-不是 1-是外链")
    @TableField("is_frame")
    private Integer isFrame;

    @ApiModelProperty("是否缓存 0-不缓存 1-缓存")
    @TableField("is_cache")
    private Integer isCache;

    @ApiModelProperty("菜单接入类型（1-管理权限，2-业务权限，3-审计权限）")
    @TableField("access_type")
    private Integer accessType;

    @ApiModelProperty("菜单组件")
    @TableField("component")
    @JsonFormat
    private String component;


}
