package com.tc.modules.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author dujingrui
 * @Data 2025/3/18 17:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemperatureExportQuery implements Serializable {

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 告警id
     */
    private Integer alarmId;

    /**
     * 站点id
     */
    private  Integer siteId;

    /**
     * 设备id
     */
    private Integer hardwareId;
}
