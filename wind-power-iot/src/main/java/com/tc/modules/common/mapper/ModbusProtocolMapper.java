package com.tc.modules.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tc.modules.common.entity.TCameraArea;
import com.tc.modules.common.entity.TModbusProtocol;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ModbusProtocolMapper extends BaseMapper<TModbusProtocol> {
}
