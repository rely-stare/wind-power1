package com.tc.modules.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tc.modules.entity.TCameraInfo;
import com.tc.modules.vo.TiInfoVo;

import java.util.List;


/**
 * @author sunchao
 */
public interface TCameraInfoService extends IService<TCameraInfo> {


    TCameraInfo getCameraInfoByHardwareId(Integer id);

    List<TCameraInfo> getTCameraInfoByOrgIdAndMonitorType(Integer orgId, Integer monitorType);

    List<TCameraInfo> getHardwareCameraInfo(int siteId, int hardwareType);

    TiInfoVo getTiInfoVoList(Integer siteId, Integer hardwareType, Integer monitorType);

}
