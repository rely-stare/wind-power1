package com.tc.modules.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tc.modules.entity.TAudioInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TAudioInfoMapper extends BaseMapper<TAudioInfo> {

    List<TAudioInfo> getTAudioInfoByOrgIdAndMonitorType(@Param("orgId") Integer orgId, @Param("monitorType") Integer monitorType);


    TAudioInfo getTAudioInfoByCode(@Param("code") String code);
}
