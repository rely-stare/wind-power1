package com.tc.modules.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tc.modules.common.entity.TCameraArea;
import com.tc.modules.common.entity.TCameraNrvInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CameraAreaMapper extends BaseMapper<TCameraArea> {

    TCameraNrvInfo getCameraNvrByHardwareId(int hardwareId);
}
