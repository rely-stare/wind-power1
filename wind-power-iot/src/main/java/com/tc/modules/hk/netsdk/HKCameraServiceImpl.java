package com.tc.modules.hk.netsdk;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.tc.modules.hk.service.HKCameraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class HKCameraServiceImpl implements HKCameraService {

    private final HCNetSDKManager sdkManager;
    private final PlayCtrlManager playCtrlManager;

    private final Map<Integer, RealDataCallback> callbackMap = new ConcurrentHashMap<>();

    private final Map<Integer, Integer> hardwareHandleMap = new ConcurrentHashMap<>();


    @Override
    public int startPreview(int userId, int channelNo) {
        HCNetSDK sdk = sdkManager.getSDKInstance();
        IntByReference port = new IntByReference(-1);

        HCNetSDK.NET_DVR_PREVIEWINFO previewInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
        previewInfo.read();
        previewInfo.lChannel = channelNo;
        previewInfo.dwStreamType = 0;
        previewInfo.dwLinkMode = 0;
        previewInfo.bBlocked = 1;
        previewInfo.byProtoType = 0;
        previewInfo.write();

        PlayCtrl playCtrl = playCtrlManager.createPlayCtrl();
        RealDataCallback callback = new RealDataCallback(playCtrl, port);

        int playHandle = sdk.NET_DVR_RealPlay_V40(userId, previewInfo, callback, null);
        if (playHandle == -1) {
            throw new RuntimeException("取流失败，错误码：" + sdk.NET_DVR_GetLastError());
        }

        playCtrlManager.bindPlayHandle(playHandle, playCtrl,port);
        callbackMap.put(playHandle, callback);

        return playHandle;
    }

    @Override
    public String capturePicture(int playHandle) {

        PlayCtrl playCtrl = playCtrlManager.getPlayCtrl(playHandle);
        IntByReference port = playCtrlManager.getPort(playHandle);

        IntByReference pWidth = new IntByReference(0);
        IntByReference pHieght = new IntByReference(0);
        if (!playCtrl.PlayM4_GetPictureSize(port.getValue(), pWidth, pHieght)) {
            log.error("获取失败：{}", playCtrl.PlayM4_GetLastError(port.getValue()));
//            throw new RuntimeException("获取图像尺寸失败");
        }

        IntByReference RealPicSize = new IntByReference(0);
        int picsize = pWidth.getValue() * pHieght.getValue() * 5;

        HCNetSDK.BYTE_ARRAY picByte = new HCNetSDK.BYTE_ARRAY(picsize);
        picByte.write();
        Pointer pByte = picByte.getPointer();
        if (!playCtrl.PlayM4_GetJPEG(port.getValue(), pByte, picsize, RealPicSize)) {
            throw new RuntimeException("抓拍失败");
        }

        picByte.read();
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
        String newName = sf.format(new Date());
        FileOutputStream fout = null;
        String fileName = System.getProperty("user.dir") + "//snapshots//" + newName + ".jpg";
        try {

            fout = new FileOutputStream(fileName);
            //将字节写入文件
            long offset = 0;
            ByteBuffer buffers = pByte.getByteBuffer(offset, RealPicSize.getValue());
            byte[] bytes = new byte[RealPicSize.getValue()];
            buffers.rewind();
            buffers.get(bytes);
            fout.write(bytes);
            fout.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        log.info("抓拍成功：{}", fileName);

        return fileName;
    }

    @Override
    public void stopPreview(int playHandle) {
        HCNetSDK sdk = sdkManager.getSDKInstance();
        sdk.NET_DVR_StopRealPlay(playHandle);
    }


    public int getHandle(int hardwareId) {
        return hardwareHandleMap.get(hardwareId);
    }

    public void putHandle(int hardwareId, int handle) {
        hardwareHandleMap.put(hardwareId, handle);
        log.info("hardwareHandleMap:{}", hardwareHandleMap);
    }

    private static class RealDataCallback implements HCNetSDK.FRealDataCallBack_V30 {

        private final PlayCtrl playCtrl;
        private final IntByReference m_lPort;

        public RealDataCallback(PlayCtrl playCtrl, IntByReference port) {
            this.playCtrl = playCtrl;
            this.m_lPort = port;
        }

        @Override
        public void invoke(int lRealHandle, int dwDataType, Pointer pBuffer, int dwBufSize, Pointer pUser) {
            try {
                switch (dwDataType) {
                    case HCNetSDK.NET_DVR_SYSHEAD: //系统头
                        if (!playCtrl.PlayM4_GetPort(m_lPort)) //获取播放库未使用的通道号
                        {
                            break;
                        }
                        if (dwBufSize > 0) {
                            if (!playCtrl.PlayM4_SetStreamOpenMode(m_lPort.getValue(), PlayCtrl.STREAME_REALTIME))  //设置实时流播放模式
                            {
                                break;
                            }
                            if (!playCtrl.PlayM4_OpenStream(m_lPort.getValue(), pBuffer, dwBufSize, 1024 * 1024)) //打开流接口
                            {
                                break;
                            }
                            if (!playCtrl.PlayM4_Play(m_lPort.getValue(), null)) //播放开始
                            {
                                break;
                            }

                        }
                    case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
                        if ((dwBufSize > 0) && (m_lPort.getValue() != -1)) {
                            if (!playCtrl.PlayM4_InputData(m_lPort.getValue(), pBuffer, dwBufSize))  //输入流数据
                            {
                                break;
                            }
                        }
                }
            } catch (Exception e) {
                log.error("实时回调处理失败", e);
            }
        }
    }
}
