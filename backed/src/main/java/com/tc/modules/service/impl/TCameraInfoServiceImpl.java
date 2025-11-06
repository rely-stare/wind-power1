package com.tc.modules.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.modules.entity.TCameraInfo;
import com.tc.modules.mapper.TCameraInfoMapper;
import com.tc.modules.service.TCameraInfoService;
import com.tc.modules.vo.TiInfoVo;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author sunchao
 * @description 针对表【t_nvr_camera(视频主机摄像头)】的数据库操作Service实现
 * @createDate 2023-11-13 13:47:15
 */
@Service
public class TCameraInfoServiceImpl extends ServiceImpl<TCameraInfoMapper, TCameraInfo>
        implements TCameraInfoService {

    @Override
    public TCameraInfo getCameraInfoByHardwareId(Integer id) {
        return baseMapper.selectOne(new LambdaQueryWrapper<TCameraInfo>().eq(TCameraInfo::getHardwareId, id));
    }

    @Override
    public List<TCameraInfo> getTCameraInfoByOrgIdAndMonitorType(Integer orgId, Integer monitorType) {
        return baseMapper.getTCameraInfoByOrgIdAndMonitorType(orgId, monitorType);
    }

    @Override
    public List<TCameraInfo> getHardwareCameraInfo(int siteId, int hardwareType) {
        return baseMapper.getHardwareCameraInfo(siteId, hardwareType);
    }

    @Override
    public TiInfoVo getTiInfoVoList(Integer siteId, Integer hardwareType, Integer monitorType) {
        return baseMapper.getTiInfoVoList(siteId, hardwareType, monitorType);
    }


}




