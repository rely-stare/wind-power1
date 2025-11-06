package com.tc.modules.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.modules.entity.THardwareTemplate;
import com.tc.modules.mapper.THardwareTemplateMapper;
import com.tc.modules.service.THardwareTemplateService;
import org.springframework.stereotype.Service;

@Service
public class THardwareTemplateServiceImpl extends ServiceImpl<THardwareTemplateMapper, THardwareTemplate> implements THardwareTemplateService {
}
