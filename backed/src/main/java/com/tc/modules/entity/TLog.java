package com.tc.modules.entity;

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
 * <p>
 *
 * </p>
 *
 * @author Jz
 * @since 2023-10-17
 */
@Data
@TableName("t_log")
@ApiModel(value = "TLog对象", description = "")
public class TLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户id")
    @TableId
    private Integer userId;

    @ApiModelProperty("请求ip")
    @TableField("ip_address")
    private String ipAddress;

    @ApiModelProperty("访问实例")
    @TableField("target_server")
    private String targetServer;

    @ApiModelProperty("请求路径")
    @TableField("request_path")
    private String requestPath;

    @ApiModelProperty("请求内容")
    @TableField("request_data")
    private String requestData;

    @ApiModelProperty("请求时间")
    @TableField("request_time")
    private Date requestTime;

    @ApiModelProperty("请求时长")
    @TableField("execute_time")
    private Long executeTime;

    @ApiModelProperty("返回的状态码")
    @TableField("response_code")
    private Integer responseCode;

    @NotNull(message = "页码为空")
    @TableField(exist = false)
    private Integer page;

    @NotNull(message = "条数为空")
    @TableField(exist = false)
    @Min(value = 1, message = "最少查询1条")
    @Max(value = 100, message = "最多查询100条")
    private Integer size;

    @TableField(exist = false)
    private Integer[] responseCodes;

}
