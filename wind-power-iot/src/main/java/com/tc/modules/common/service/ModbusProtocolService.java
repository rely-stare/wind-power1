package com.tc.modules.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tc.modules.common.entity.TModbusProtocol;

public interface ModbusProtocolService extends IService<TModbusProtocol> {

    TModbusProtocol getModbusProtocolCacheByfiled(String filed);

}
