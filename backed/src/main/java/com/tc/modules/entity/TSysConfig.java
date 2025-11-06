package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author Jz
 * @since 2023-11-03
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_sys_config")
@ApiModel(value = "TSysConfig对象", description = "")
public class TSysConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("配置key")
    @TableField("param_key")
    private String paramKey;

    @ApiModelProperty("配置value")
    @TableField("param_value")
    private String paramValue;

    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty("配置类型：sys-系统配置")
    @TableField("type")
    private String type;


}
