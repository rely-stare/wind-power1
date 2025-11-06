package com.tc.modules.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.tc.modules.export.convert.DateToStringConverter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;


@Data
@AllArgsConstructor
public class LongTimeTemperatureExport {

    @ExcelProperty(value = "Time", index = 0, converter = DateToStringConverter.class)
    private Date createTime;

    @ExcelProperty(value = "Temperature", index = 1)
    private String temperature;

}
