package com.tc.modules.hk.controller;


import cn.hutool.core.date.DateUtil;
import com.tc.common.vo.Result;
import com.tc.modules.common.entity.TCameraNrvInfo;
import com.tc.modules.common.service.CameraService;
import com.tc.modules.hk.netsdk.HCNetSDKManager;
import com.tc.modules.hk.service.HKCameraService;
import com.tc.modules.hk.service.NvrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/camera")
public class CameraController {

    @Autowired
    private  HKCameraService hkCameraService;

    @Autowired
    private NvrService nvrService;

    @Autowired
    private HCNetSDKManager hCNetSDKManager;

    @Autowired
    private CameraService cameraService;

    @RequestMapping("/capturePicture")
    public Result<?> capturePicture(int deviceId) {
        int handle = hkCameraService.getHandle(deviceId);
        String fileName = hkCameraService.capturePicture(handle);
        return Result.ok(fileName);
    }

    @RequestMapping("/startPreview")
    public Result<?> startPreview(int userid) {
        int handle = hkCameraService.startPreview(userid, 1);
        hkCameraService.putHandle(19, handle);
        return Result.ok();
    }

    @RequestMapping("/downloadRecordByTime")
    public Result<?> downloadRecordByTime(int hardwareId, String startTime, String endTime, String fileName) {
        TCameraNrvInfo cameraNvr = cameraService.getCameraNvrByHardwareId(hardwareId);
        int lUserID = nvrService.nvrLogin(cameraNvr.getNvrIp(), (short) 8000, cameraNvr.getNvrUser(), cameraNvr.getNvrPsw());
        nvrService.downloadRecordByTime(lUserID, 32 + cameraNvr.getNvrChannelId(), DateUtil.parse(startTime), DateUtil.parse(endTime), fileName);
        if (hCNetSDKManager.getSDKInstance().NET_DVR_Logout(lUserID)) {
            log.info("注销成功");
        }
        return Result.ok();
    }
}
