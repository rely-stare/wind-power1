package com.tc.modules.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.modules.common.entity.THardware;
import com.tc.modules.common.mapper.HardwareMapper;
import com.tc.modules.common.service.HardwareService;
import com.tc.modules.hk.entity.DeviceInfoEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HardwareServiceImpl extends ServiceImpl<HardwareMapper, THardware> implements HardwareService {

    @Override
    public List<DeviceInfoEntity> getTiList() {
        return baseMapper.getTiList();
    }
}
