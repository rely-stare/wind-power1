package com.tc.modules.export;


import com.alibaba.excel.annotation.ExcelProperty;
import com.tc.modules.export.convert.DateToStringConverter;
import lombok.Data;

import java.util.Date;

@Data
public class SpeedFluctuationExport {

    @ExcelProperty(value = "Time", index = 0, converter = DateToStringConverter.class)
    private Date createTime;

    @ExcelProperty(value = "Site ID", index = 1)
    private Integer siteId;

    @ExcelProperty(value = "GeneratorSpeedDelay", index = 2)
    private Double speedDelay;

    @ExcelProperty(value = "GeneratorSpeedAverageDelay", index = 3)
    private Double speedAverageDelay;

}
