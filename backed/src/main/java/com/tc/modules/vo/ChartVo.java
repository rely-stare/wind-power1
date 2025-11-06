package com.tc.modules.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author sunchao
 * @date 2024/3/25 14:05
 */
@Data
public class ChartVo {

    private Integer id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date time;

    private BigDecimal speed;

    private BigDecimal frequency;

    private BigDecimal range;

    private BigDecimal rms;

    private BigDecimal maxRms;

    private BigDecimal minRms;

    private BigDecimal base;

    public BigDecimal[] toArray() {
        return new BigDecimal[]{frequency, range};
    }
}
