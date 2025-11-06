package com.tc.modules.export.convert;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;

import java.util.Date;

public class DateToStringConverter implements Converter<Date> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Date.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.DATE;
    }

    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Date> context) {
        return new WriteCellData<>(DateUtil.format(context.getValue(), "yyyy-MM-dd HH:mm:ss.SSS"));
    }
}
