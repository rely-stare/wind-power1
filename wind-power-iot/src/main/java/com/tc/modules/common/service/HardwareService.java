package com.tc.modules.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tc.modules.common.entity.THardware;
import com.tc.modules.hk.entity.DeviceInfoEntity;

import java.util.List;

public interface HardwareService extends IService<THardware> {

    List<DeviceInfoEntity> getTiList();



}
