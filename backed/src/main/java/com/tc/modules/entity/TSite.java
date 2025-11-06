package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 站点表
 * </p>
 *
 * @author Jz
 * @since 2023-12-11
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_site")
@ApiModel(value = "TSite对象", description = "站点表")
public class TSite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("风机名称")
    @TableField("site_name")
    private String siteName;

    @ApiModelProperty("风机型号")
    @TableField("site_model")
    private String siteModel;

    @ApiModelProperty("组织id（t_org）")
    @TableField("org_id")
    private Integer orgId;

    @ApiModelProperty("0-正常，1-已删除")
    @TableField("is_delete")
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;

    @ApiModelProperty("创建者id")
    @TableField("create_by")
    private Integer createBy;

    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty("组织名称")
    @TableField(exist=false)
    private String orgName;

}
