package com.tc.modules.export;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpeedSpectrumExport {

    @ExcelProperty(value = "xAxis", index = 0)
    private String xAxis;

    @ExcelProperty(value = "YAxis", index = 1)
    private String YAxis;

}
