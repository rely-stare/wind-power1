package com.tc.modules.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("t_config_center")
@ApiModel(value = "TConfigCenter", description = "")
public class TConfigCenter {
    /**
     * 主键 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 父级 id
     */
    @ApiModelProperty("父级 id")
    private Integer parentId;
    /**
     * 菜单名称
     */
    @ApiModelProperty("菜单名称")
    private String menuName;
    /**
     * 菜单编码
     */
    @ApiModelProperty("菜单编码")
    private Integer menuCode;
    /**
     * 菜单类型
     */
    @ApiModelProperty("菜单类型 0-目录，1-菜单")
    private Integer menuType;
}
