package com.tc.modules.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 配置规则实体类
 */
@Data
@TableName("t_config_rule")
@ApiModel(value = "TConfigRule", description = "")
public class TConfigRule implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 设备类型
     */
    private Integer hardwareType;
    /**
     * 菜单代码
     */
    private Integer menuCode;
    /**
     * 功能代码
     */
    private Integer monitorType;
    /**
     * 功能名称
     */
    private String funcName;
    /**
     * 报警名称
     */
    private String alarmName;
    /**
     * 报警级别
     */
    private Integer alarmLevel;
    /**
     * 比较符号
     */
    private String symbol;
    /**
     * 阈值
     */
    private String threshold;
    /**
     * 单位
     */
    private String unit;
    /**
     *  modbus字段
     */
    private String modbusField;
    /**
     * 删除标志
     */
    private Integer isDelete;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}
