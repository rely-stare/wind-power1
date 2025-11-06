package com.tc.modules.hk.service.impl;

import cn.hutool.core.date.DateUtil;
import com.sun.jna.ptr.IntByReference;
import com.tc.modules.hk.netsdk.HCNetSDK;
import com.tc.modules.hk.netsdk.HCNetSDKManager;
import com.tc.modules.hk.service.NvrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;


@Service
@Slf4j
public class NvrServiceImpl implements NvrService {

    @Autowired
    private HCNetSDKManager hCNetSDKManager;

    @Override
    public int nvrLogin(String ip, short port, String user, String psw) {
        return hCNetSDKManager.loginDevice(ip, port, user, psw);
    }

    @Override
    public void downloadRecordByTime(int userID, int iChannelNo, Date startTime, Date endTime, String fileName) {
        HCNetSDK hCNetSDK = hCNetSDKManager.getSDKInstance();

        HCNetSDK.NET_DVR_PLAYCOND net_dvr_playcond = new HCNetSDK.NET_DVR_PLAYCOND();
        net_dvr_playcond.read();
        net_dvr_playcond.dwChannel = iChannelNo; //通道号 NVR设备路数小于32路的起始通道号从33开始，依次增加
        //开始时间
        net_dvr_playcond.struStartTime.dwYear = DateUtil.year(startTime);
        net_dvr_playcond.struStartTime.dwMonth = DateUtil.month(startTime);
        net_dvr_playcond.struStartTime.dwDay = DateUtil.dayOfMonth(startTime);
        net_dvr_playcond.struStartTime.dwHour = DateUtil.hour(startTime, true);
        net_dvr_playcond.struStartTime.dwMinute = DateUtil.minute(startTime);
        net_dvr_playcond.struStartTime.dwSecond = 0;
        //停止时间
        net_dvr_playcond.struStopTime.dwYear = DateUtil.year(endTime);
        net_dvr_playcond.struStopTime.dwMonth = DateUtil.month(endTime);
        net_dvr_playcond.struStopTime.dwDay = DateUtil.dayOfMonth(endTime);
        net_dvr_playcond.struStopTime.dwHour = DateUtil.hour(endTime, true);
        net_dvr_playcond.struStopTime.dwMinute = DateUtil.minute(endTime);
        net_dvr_playcond.struStopTime.dwSecond = 0;
        net_dvr_playcond.write();
        int m_lLoadHandle = hCNetSDK.NET_DVR_GetFileByTime_V40(userID, fileName, net_dvr_playcond);
        if (m_lLoadHandle >= 0) {
            hCNetSDK.NET_DVR_PlayBackControl(m_lLoadHandle, HCNetSDK.NET_DVR_PLAYSTART, 0, null);
            Date nowTime = new Date(System.currentTimeMillis());
            SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
            log.info("开始下载时间：{}", sdFormatter.format(nowTime));

            CountDownLatch latch = new CountDownLatch(1);

            Timer downloadtimer = new Timer();//新建定时器
            downloadtimer.schedule(new downloadTask(m_lLoadHandle, downloadtimer, latch), 0, 5000);//0秒后开始响应函数

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } else {
            log.error("按时间下载失败 !  last error {}", hCNetSDK.NET_DVR_GetLastError());
        }
    }


    public class downloadTask extends java.util.TimerTask {

        private int m_lLoadHandle;
        private Timer downloadtimer;
        private CountDownLatch latch;

        public downloadTask(int m_lLoadHandle, Timer downloadtimer,CountDownLatch latch) {
            this.m_lLoadHandle = m_lLoadHandle;
            this.downloadtimer = downloadtimer;
            this.latch = latch;
        }

        //定时器函数
        @Override
        public void run() {
            HCNetSDK hCNetSDK = hCNetSDKManager.getSDKInstance();

            IntByReference nPos = new IntByReference(0);
            hCNetSDK.NET_DVR_PlayBackControl(m_lLoadHandle, HCNetSDK.NET_DVR_PLAYGETPOS, 0, nPos);
            if (nPos.getValue() > 100) {
                m_lLoadHandle = -1;
                downloadtimer.cancel();
                log.error("由于网络原因或DVR忙,下载异常终止!");
            }
            if (nPos.getValue() == 100) {
                Date nowTime = new Date(System.currentTimeMillis());
                SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
                log.info("结束下载时间：{}", sdFormatter.format(nowTime));
                m_lLoadHandle = -1;
                // 任务完成后，取消定时器
                // 取消任务，防止再次执行
                latch.countDown();
                this.cancel();
                // 取消定时器
                if (downloadtimer != null) {
                    downloadtimer.cancel();
                }
            }
        }
    }
}
