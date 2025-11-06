package com.tc.modules.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tc.modules.entity.TCameraInfo;
import com.tc.modules.vo.TiInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author sunchao
 */
@Mapper
public interface TCameraInfoMapper extends BaseMapper<TCameraInfo> {

    List<TCameraInfo> getTCameraInfoByOrgIdAndMonitorType(@Param("orgId") Integer orgId, @Param("monitorType") Integer monitorType);

    List<TCameraInfo> getHardwareCameraInfo(@Param("siteId") int siteId, @Param("hardwareType") int hardwareType);

    TiInfoVo getTiInfoVoList(@Param("siteId") Integer siteId, @Param("hardwareType") Integer hardwareType, @Param("monitorType") Integer monitorType);
}




