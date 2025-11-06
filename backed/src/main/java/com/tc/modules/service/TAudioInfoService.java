package com.tc.modules.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tc.modules.entity.TAudioInfo;

import java.util.List;

public interface TAudioInfoService extends IService<TAudioInfo> {

    List<TAudioInfo> getTAudioInfoByOrgIdAndMonitorType(Integer orgId, Integer monitorType);

    TAudioInfo getAudioInfoByCode(String hardwareId);

}
