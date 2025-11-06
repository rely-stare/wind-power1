package com.tc.modules.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tc.modules.entity.TAlarm;
import com.tc.modules.po.AlarmPo;
import com.tc.modules.query.AlarmQuery;
import com.tc.modules.vo.AlarmFactionVo;
import com.tc.modules.vo.AlarmSiteVo;
import com.tc.modules.vo.AlarmVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author sunchao
* @description 针对表【t_alarm(告警记录表)】的数据库操作Mapper
* @createDate 2024-03-05 15:38:11
* @Entity com.tc.modules.entity.TAlarm
*/
@Mapper
public interface TAlarmMapper extends BaseMapper<TAlarm> {

    Page<TAlarm> getAlarmPage(Page<TAlarm> page,
                              @Param("query") AlarmQuery query,
                              @Param("siteIds") List<Integer> siteIds);

}
