package com.tc.modules.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.modules.entity.TConfigRule;
import com.tc.modules.mapper.TConfigRuleMapper;
import com.tc.modules.service.TConfigRuleService;
import org.springframework.stereotype.Service;

@Service
public class TConfigRuleServiceImpl extends ServiceImpl<TConfigRuleMapper, TConfigRule> implements TConfigRuleService {
}
