package com.tc.modules.hk.service;

public interface HKCameraService {

    int startPreview(int userId, int channelNo);

    String capturePicture(int playHandle);

    void stopPreview(int playHandle);

    public int getHandle(int hardwareId);

    public void putHandle(int hardwareId, int handle);
}
