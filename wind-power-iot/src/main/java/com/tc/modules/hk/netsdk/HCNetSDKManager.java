package com.tc.modules.hk.netsdk;


import com.sun.jna.Native;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
@Slf4j
public class HCNetSDKManager {

    private HCNetSDK hCNetSDKInstance;

    public void init() {
        getSDKInstance();
    }

    // 初始化 SDK 实例
    public HCNetSDK getSDKInstance() {
        if (hCNetSDKInstance != null) {
            return hCNetSDKInstance;
        }

        synchronized (HCNetSDK.class) {
            if (hCNetSDKInstance != null) {
                return hCNetSDKInstance;
            }
            String strDllPath = getLibraryPath();
            try {
                hCNetSDKInstance = (HCNetSDK) Native.loadLibrary(strDllPath, HCNetSDK.class);
                log.info("Successfully loaded library from: {}", strDllPath);
            } catch (Exception ex) {
                log.error("Failed to load library from {}. Error: {}", strDllPath, ex.getMessage(), ex);
                throw new RuntimeException("Failed to initialize SDK instance", ex);
            }

            String osName = System.getProperty("os.name").toLowerCase();
            String userDir = System.getProperty("user.dir");
            if (osName.contains("linux")) {
                HCNetSDK.BYTE_ARRAY ptrByteArray1 = new HCNetSDK.BYTE_ARRAY(256);
                HCNetSDK.BYTE_ARRAY ptrByteArray2 = new HCNetSDK.BYTE_ARRAY(256);
                //这里是库的绝对路径，请根据实际情况修改，注意改路径必须有访问权限
                String strPath1 = Paths.get(userDir, "lib", "hk", "linux", "libcrypto.so.1.1").toString();
                String strPath2 = Paths.get(userDir, "lib", "hk", "linux", "libssl.so.1.1").toString();
                System.arraycopy(strPath1.getBytes(), 0, ptrByteArray1.byValue, 0, strPath1.length());
                ptrByteArray1.write();
                hCNetSDKInstance.NET_DVR_SetSDKInitCfg(HCNetSDK.NET_SDK_INIT_CFG_LIBEAY_PATH, ptrByteArray1.getPointer());
                System.arraycopy(strPath2.getBytes(), 0, ptrByteArray2.byValue, 0, strPath2.length());
                ptrByteArray2.write();
                hCNetSDKInstance.NET_DVR_SetSDKInitCfg(HCNetSDK.NET_SDK_INIT_CFG_SSLEAY_PATH, ptrByteArray2.getPointer());
                String strPathCom = System.getProperty("user.dir") + "/lib/hk/linux";
                HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
                System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
                struComPath.write();
                hCNetSDKInstance.NET_DVR_SetSDKInitCfg(HCNetSDK.NET_SDK_INIT_CFG_SDK_PATH, struComPath.getPointer());
            }
        }
        return hCNetSDKInstance;
    }

    // 获取动态库路径
    private static String getLibraryPath() {
        String osName = System.getProperty("os.name").toLowerCase();
        String userDir = System.getProperty("user.dir");
        String libPath;

        if (osName.contains("win")) {
            libPath = Paths.get(userDir, "lib", "hk", "win", "HCNetSDK.dll").toString();
        } else if (osName.contains("linux")) {
            libPath = Paths.get(userDir, "lib", "hk", "linux", "libhcnetsdk.so").toString();
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + osName);
        }

        return libPath;
    }

    // 登录设备
    public int loginDevice(String ip, short port, String user, String psw) {
        if (hCNetSDKInstance == null) {
            throw new IllegalArgumentException("HCNetSDK instance is null. Please initialize it first.");
        }

        HCNetSDK.NET_DVR_USER_LOGIN_INFO loginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
        HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();

        // 设置设备IP地址
        byte[] deviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        byte[] ipBytes = ip.getBytes();
        System.arraycopy(ipBytes, 0, deviceAddress, 0, Math.min(ipBytes.length, deviceAddress.length));
        loginInfo.sDeviceAddress = deviceAddress;

        // 设置用户名和密码
        byte[] userName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        byte[] password = psw.getBytes();
        System.arraycopy(user.getBytes(), 0, userName, 0, Math.min(user.length(), userName.length));
        System.arraycopy(password, 0, loginInfo.sPassword, 0, Math.min(password.length, loginInfo.sPassword.length));
        loginInfo.sUserName = userName;

        // 设置端口和登录模式
        loginInfo.wPort = port;
        loginInfo.bUseAsynLogin = false; // 同步登录
        loginInfo.byLoginMode = 0; // 使用SDK私有协议

        // 执行登录操作
        int userID = hCNetSDKInstance.NET_DVR_Login_V40(loginInfo, deviceInfo);
        if (userID == -1) {
            log.error("设备 {}:{} 登录失败，错误码: {}", ip, port, hCNetSDKInstance.NET_DVR_GetLastError());
        } else {
            int startDChan = deviceInfo.struDeviceV30.byStartDChan;
            log.info("设备 {}:{} 登录成功，预览起始通道号: {}", ip, port, startDChan);
        }
        return userID;
    }
}
