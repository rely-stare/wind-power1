package com.tc.modules.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.modules.entity.THardware;
import com.tc.modules.mapper.THardwareMapper;
import com.tc.modules.service.THardwareService;
import org.springframework.stereotype.Service;

@Service
public class THardwareServiceImpl extends ServiceImpl<THardwareMapper, THardware> implements THardwareService {
}
