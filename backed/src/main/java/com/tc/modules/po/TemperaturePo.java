package com.tc.modules.po;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

@Data
public class TemperaturePo {

    private int hardwareId ;
    /**
     * 摄像头id
     */
    private int cameraId;
    /**
     * 摄像头通道id
     */
    private int channelId;
    /**
     * 规则类型
     */
    private int ruleType;
    /**
     * 规则id
     */
    private int ruleId;
    /**
     * 规则名称
     */
    private String ruleName;
    /**
     * 温度
     */
    private String currentTemperature;
    /**
     * 最高温度
     */
    private String maxTemperature;
    /**
     * 最低温度
     */
    private String minTemperature;
    /**
     * 平均温度
     */
    private String aveTemperature;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
