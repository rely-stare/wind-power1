package com.tc.run;

import com.tc.modules.common.service.HardwareService;
import com.tc.modules.hk.entity.DeviceInfoEntity;
import com.tc.modules.hk.netsdk.HCNetSDK;
import com.tc.modules.hk.netsdk.HCNetSDKManager;
import com.tc.modules.hk.service.HKCameraService;
import com.tc.modules.hk.service.TemperatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Component
public class HKMonitorRunner implements ApplicationRunner {

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Autowired
    private TemperatureService temperatureService;

    @Autowired
    private HardwareService hardwareService;

    @Autowired
    private HCNetSDKManager hcNetSDKManager;

    @Autowired
    private HKCameraService hkCameraService;

    @Override
    public void run(ApplicationArguments args) {

        hcNetSDKManager.init();
        // 获取 SDK 实例
        HCNetSDK sdkInstance = hcNetSDKManager.getSDKInstance();
        sdkInstance.NET_DVR_Init();
        sdkInstance.NET_DVR_SetConnectTime(2000, 1);
        sdkInstance.NET_DVR_SetReconnect(10000, true);
        sdkInstance.NET_DVR_SetLogToFile(3, "./sdklog", false);

        List<DeviceInfoEntity> deviceInfoList = hardwareService.getTiList();

        for (DeviceInfoEntity deviceInfo : deviceInfoList) {
            executorService.submit(() -> {
                int userId = hcNetSDKManager.loginDevice(deviceInfo.getIp(), (short) 8000, deviceInfo.getUsername(), deviceInfo.getPassword());
                // 开启测温
                temperatureService.startTemperature(userId, deviceInfo);
//                // 开启预览
//                int playHandle = hkCameraService.startPreview(userId, 1);
//
//                hkCameraService.putHandle(deviceInfo.getHardwareId(), playHandle);
            });
        }
    }

//    @PreDestroy
//    public void destroy() {
//        log.info("销毁 SDK 资源...");
//        HCNetSDK sdkInstance = HCNetSDKManager.getSDKInstance();
//        if (sdkInstance != null) {
//            sdkInstance.NET_DVR_Cleanup();
//        }
//        log.info("SDK 资源销毁完成");
//    }
}