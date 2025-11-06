package com.tc.modules.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tc.modules.common.entity.TCameraArea;
import com.tc.modules.common.entity.TCameraNrvInfo;

public interface CameraService extends IService<TCameraArea> {

    public TCameraNrvInfo getCameraNvrByHardwareId(int hardwxareId);
}
