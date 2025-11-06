package com.tc.modules.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.modules.entity.TAudioInfo;
import com.tc.modules.mapper.TAudioInfoMapper;
import com.tc.modules.service.TAudioInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TAudioInfoServiceImpl extends ServiceImpl<TAudioInfoMapper, TAudioInfo> implements TAudioInfoService {
    @Override
    public List<TAudioInfo> getTAudioInfoByOrgIdAndMonitorType(Integer orgId, Integer monitorType) {
        return baseMapper.getTAudioInfoByOrgIdAndMonitorType(orgId, monitorType);
    }

    @Override
    public TAudioInfo getAudioInfoByCode(String code) {
        return baseMapper.getTAudioInfoByCode(code);
    }

}
