package com.tc.modules.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tc.common.enums.MonitorType;
import com.tc.modules.entity.TSpeedData;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface TSpeedDataService extends IService<TSpeedData> {

    List<TSpeedData> getSpeedDataByInfluxDB(int siteId, Date startTime, Date endTime);

    void frequencyAnalyse(List<TSpeedData> data, int siteId, Date activeTime);

    /**
     * 导出speed数据
     *
     * @param siteId，endTime
     * @return
     */
    ResponseEntity<byte[]> exportSpeedData(int siteId, Date endTime, Integer monitorType, Integer alarmId) throws IOException;
}
