package com.tc.modules.hk.service;

import java.util.Date;

public interface NvrService {

    /**
     * 登录NVR（Network Video Recorder）设备

     * @param ip   NVR设备的IP地址
     * @param port NVR设备的端口号，
     * @param user 用户名，用于身份验证
     * @param psw  密码，用于身份验证
     * @return 返回一个整型值，表示登录状态或结果
     */
    int nvrLogin(String ip, short port, String user, String psw);


    /**
     * 根据时间下载文件
     *
     * @param userID 用户ID，用于标识记录所属的用户
     * @param iChannelNo 频道号，用于标识记录所属的频道
     * @param startTime 开始时间，定义记录文件的开始时间界限
     * @param endTime 结束时间，定义记录文件的结束时间界限
     * @param fileName 文件名，指定要下载的记录文件的名称
     */
    void downloadRecordByTime(int userID, int iChannelNo, Date startTime, Date endTime, String fileName);
}
