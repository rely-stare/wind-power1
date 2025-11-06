package com.tc.modules.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tc.modules.entity.TTemperatureData;
import com.tc.modules.query.CommonQuery;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface TTemperatureDataService extends IService<TTemperatureData> {

    /**
     * 从InfluxDB查询条件获取温度数据列表
     *
     * @param query 共通查询对象，包含查询条件和参数
     * @return 温度数据列表，包含符合查询条件的温度记录
     */
    List<TTemperatureData> getTemperatureDataListByInfluxDB(CommonQuery query);
    /**
     * 温度导出excel
     *
     * @param
     * @return
     */
    ResponseEntity<byte[]> temperatureExport(Date endTime, Integer alarmId , Integer siteId,Integer hardwareId) throws IOException;
}
