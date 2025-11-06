package com.tc.modules.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.modules.common.entity.TCameraArea;
import com.tc.modules.common.entity.TCameraNrvInfo;
import com.tc.modules.common.mapper.CameraAreaMapper;
import com.tc.modules.common.service.CameraService;
import org.springframework.stereotype.Service;


@Service
public class CameraServiceImpl extends ServiceImpl<CameraAreaMapper, TCameraArea> implements CameraService {
    @Override
    public TCameraNrvInfo getCameraNvrByHardwareId(int hardwareId) {
        return baseMapper.getCameraNvrByHardwareId(hardwareId);
    }
}
