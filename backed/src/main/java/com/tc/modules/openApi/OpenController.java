package com.tc.modules.openApi;


import cn.hutool.core.date.DateUtil;
import com.tc.common.annotation.OpenApiPermission;
import com.tc.common.enums.AlarmType;
import com.tc.common.http.DataResponse;
import com.tc.modules.entity.TAlarm;
import com.tc.modules.entity.TAudioInfo;
import com.tc.modules.entity.TSite;
import com.tc.modules.po.AlarmPo;
import com.tc.modules.service.TAudioInfoService;
import com.tc.modules.service.TSiteService;
import com.tc.modules.vo.TSiteStatusVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("open")
@Api(tags = "开放接口")
public class OpenController {

    @Autowired
    private TAudioInfoService audioInfoService;

    @Autowired
    private TSiteService siteService;


    @PostMapping("/alarm/addAlarmInfo")
    @OpenApiPermission
    @ApiOperation("添加音频告警")
    @ApiImplicitParam(name = "api-key", value = "秘钥", dataType = "String", paramType = "header", required = true)
    public DataResponse<Object> addAlarmInfo(@RequestBody AlarmPo alarmInfo) {

        String deviceCode = alarmInfo.getDeviceCode();
        TAudioInfo audioInfo = audioInfoService.getAudioInfoByCode(deviceCode);

        TAlarm alarm = new TAlarm();
        alarm.setSiteId(audioInfo.getSiteId());
        alarm.setHardwareId(audioInfo.getHardwareId() + "");
        alarm.setAlarmType(AlarmType.AUDIO_ALARM.getCode());
        alarm.setAlarmLevel(alarmInfo.getAlarmLevel());
        alarm.setMonitorLocation(audioInfo.getMonitorLocation());
        alarm.setAlarmContent(alarmInfo.getAlarmSource());
        alarm.setIsCheck(0);
        alarm.setAlarmTime(DateUtil.parse(alarmInfo.getAlarmTime()));
        return DataResponse.success();
    }


    @GetMapping("site/status")
    @OpenApiPermission
    @ApiOperation("风机状态")
    @ApiImplicitParam(name = "api-key", value = "秘钥", dataType = "String", paramType = "header", required = true)
    public DataResponse<TSiteStatusVo> status(@ApiParam(value = "风机 id") Integer siteId){

        if (siteId == null) {
            return DataResponse.fail(500, "参数错误", null);
        }

        TSite site = siteService.getById(siteId);
        if (site == null) {
            return DataResponse.fail(500, "风机不存在！", null);
        }

        boolean siteStatus = siteService.getSiteStatus(siteId);
        TSiteStatusVo siteStatusVo = new TSiteStatusVo();
        siteStatusVo.setSiteId(siteId);
        siteStatusVo.setStatus(siteStatus ? 1 : 0);
        return DataResponse.success(siteStatusVo);
    }
}
