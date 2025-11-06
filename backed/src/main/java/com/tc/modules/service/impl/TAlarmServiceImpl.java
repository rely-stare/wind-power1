package com.tc.modules.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.common.enums.AlarmType;
import com.tc.common.enums.HardwareType;
import com.tc.modules.entity.TAlarm;
import com.tc.modules.entity.TConfigRule;
import com.tc.modules.entity.THardware;
import com.tc.modules.mapper.TAlarmMapper;
import com.tc.modules.query.AlarmQuery;
import com.tc.modules.service.TAlarmService;
import com.tc.modules.service.THardwareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author sunchao
 * @description 针对表【t_alarm(告警记录表)】的数据库操作Service实现
 * @createDate 2024-03-05 15:38:11
 */
@Service
public class TAlarmServiceImpl extends ServiceImpl<TAlarmMapper, TAlarm> implements TAlarmService {

    @Autowired
    private THardwareService hardwareService;

    @Override
    public void addAlarmInfo(Integer siteId, String hardwareId, AlarmType type, TConfigRule rule, Date alarmTime, String remark) {

        String monitorLocation = null;
        Integer monitorType = null;

        if (hardwareId == null) {
            switch (type) {
                case SPEED_ALARM:
                    monitorLocation = "转速-超限";
                    monitorType = 1;
                    break;
                case STABILITY_ALARM:
                    monitorLocation = "超限-稳定性";
                    monitorType = 2;
                    break;
                case OSCILLATION_ALARM:
                    monitorLocation = "超限-震荡";
                    monitorType = 3;
                    break;
                case FREQUENCY_ALARM:
                    monitorLocation = "超限-频谱";
                    monitorType = 4;
            }

            List<THardware> list = hardwareService.list(new LambdaQueryWrapper<THardware>().eq(THardware::getSiteId, siteId));
            Integer finalMonitorType = monitorType;
            THardware hardware = list.stream().filter(item -> Objects.equals(item.getMonitorType(), finalMonitorType)).findFirst().orElse(null);
            if (hardware != null) {
                hardwareId = hardware.getId() + "";
            }
        } else {
            THardware byId = hardwareService.getById(hardwareId);
            Integer hardwareType = byId.getHardwareType();
            String message = HardwareType.getHardwareType(hardwareType).getMessage();
            monitorLocation = message + "-" + byId.getMonitorLocation();
        }

        TAlarm alarm = new TAlarm();
        alarm.setSiteId(siteId);
        alarm.setHardwareId(hardwareId);
        alarm.setAlarmType(type.getCode());
        alarm.setAlarmLevel(rule.getAlarmLevel());
        alarm.setMonitorLocation(monitorLocation);
        alarm.setAlarmContent(rule.getAlarmName());
        alarm.setIsCheck(0);
        alarm.setAlarmTime(alarmTime);
        alarm.setRemark(remark);
        baseMapper.insert(alarm);
    }

    @Override
    public void addAlarmInfo(Integer siteId, String hardwareId, AlarmType type, TConfigRule rule, Date alarmTime) {
        addAlarmInfo(siteId, hardwareId, type, rule, alarmTime,null);
    }


    @Override
    public Page<TAlarm> getAlarmPage(Page<TAlarm> page, AlarmQuery query, List<Integer> siteIds) {
        return baseMapper.getAlarmPage(page, query, siteIds);
    }
}
