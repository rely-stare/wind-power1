package com.tc.modules.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.modules.common.entity.TModbusProtocol;
import com.tc.modules.common.mapper.ModbusProtocolMapper;
import com.tc.modules.common.service.ModbusProtocolService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class ModbusProtocolServiceImpl extends ServiceImpl<ModbusProtocolMapper, TModbusProtocol> implements ModbusProtocolService {

    private final List<TModbusProtocol> cacheList = new ArrayList<>();

    @PostConstruct
    public void init() {
        List<TModbusProtocol> tModbusProtocolList = baseMapper.selectList(new LambdaQueryWrapper<>());
        cacheList.addAll(tModbusProtocolList);
    }

    @Override
    public TModbusProtocol getModbusProtocolCacheByfiled(String filed) {
        return cacheList.stream().filter(s -> s.getField().equals(filed)).findFirst().orElse(null);
    }
}

