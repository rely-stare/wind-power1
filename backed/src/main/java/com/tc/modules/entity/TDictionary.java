package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @since 2023-11-08
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_dictionary")
@ApiModel(value = "TDictionary对象", description = "")
public class TDictionary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("字典key")
    @TableField("param_key")
    private String paramKey;

    @ApiModelProperty("字典value")
    @TableField("param_value")
    private String paramValue;

    @ApiModelProperty("字典类型")
    @TableField("type")
    private String type;


}
