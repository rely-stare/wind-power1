package com.tc.modules.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tc.modules.entity.TAlarm;
import com.tc.modules.entity.TNvrInfo;
import com.tc.modules.po.AlarmPo;
import com.tc.modules.vo.AlarmFactionVo;
import com.tc.modules.vo.AlarmSiteVo;
import com.tc.modules.vo.AlarmVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface TNvrInfoMapper extends BaseMapper<TNvrInfo> {

}
