package com.tc.modules.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tc.common.enums.AlarmType;
import com.tc.modules.entity.TAlarm;
import com.tc.modules.entity.TConfigRule;
import com.tc.modules.query.AlarmQuery;

import java.util.Date;
import java.util.List;

/**
* @author sunchao
* @description 针对表【t_alarm(告警记录表)】的数据库操作Service
* @createDate 2024-03-05 15:38:11
*/
public interface TAlarmService extends IService<TAlarm> {

    void addAlarmInfo(Integer siteId, String hardwareId, AlarmType type, TConfigRule rule, Date alarmTime);

    void addAlarmInfo(Integer siteId, String hardwareId, AlarmType type, TConfigRule rule, Date alarmTime, String remark);

    Page<TAlarm> getAlarmPage(Page<TAlarm> page, AlarmQuery query, List<Integer> siteIds);

}
