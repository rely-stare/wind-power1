package com.tc.modules.hk.service;

import com.tc.modules.hk.entity.DeviceInfoEntity;

public interface TemperatureService {

    /**
     * 开始温度采集
     */
    void startTemperature(int userId, DeviceInfoEntity deviceInfo);
}
