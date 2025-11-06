package com.tc.modules.common.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tc.modules.common.entity.THardware;
import com.tc.modules.hk.entity.DeviceInfoEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface HardwareMapper extends BaseMapper<THardware> {

    List<DeviceInfoEntity> getTiList();

    List<Map<String,Object>> getSpeedInfo();

}
