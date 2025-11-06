package com.tc.modules.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.modules.common.entity.TSpeedInfo;
import com.tc.modules.common.mapper.SpeedInfoMapper;
import com.tc.modules.common.service.SpeedInfoService;
import org.springframework.stereotype.Service;

@Service
public class SpeedInfoServiceImpl extends ServiceImpl<SpeedInfoMapper, TSpeedInfo> implements SpeedInfoService {

}
