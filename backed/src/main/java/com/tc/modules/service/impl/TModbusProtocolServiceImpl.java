package com.tc.modules.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.modules.entity.TModbusProtocol;
import com.tc.modules.mapper.TModbusProtocolMapper;
import com.tc.modules.service.TModbusProtocolService;
import org.springframework.stereotype.Service;

@Service
public class TModbusProtocolServiceImpl extends ServiceImpl<TModbusProtocolMapper, TModbusProtocol> implements TModbusProtocolService {
}
