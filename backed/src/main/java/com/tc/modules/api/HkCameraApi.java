package com.tc.modules.api;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.tc.common.config.IotConfig;
import com.tc.common.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class HkCameraApi {

    @Autowired
    private IotConfig iotConfig;

    public void downloadRecordByTime(int hardwareId, Date startTime, Date endTime, String fileName){
        String url = "http://" + iotConfig.getHost() + ":" + iotConfig.getPort() + "/camera/downloadRecordByTime";
        Map<String, String> headers = new HashMap<>();
        headers.put(iotConfig.getApiKey(), iotConfig.getApiSecret());

        DateTime startTime1 = DateUtil.offset(startTime, DateField.MINUTE, -10);
        DateTime endTime1 = DateUtil.offset(endTime, DateField.MINUTE, 10);

        Map<String, String> params = new HashMap<>();
        params.put("hardwareId", hardwareId + "");
        params.put("fileName", fileName);
        params.put("startTime", DateUtil.format(startTime1, DatePattern.NORM_DATETIME_PATTERN));
        params.put("endTime", DateUtil.format(endTime1, DatePattern.NORM_DATETIME_PATTERN));

        ResponseEntity<String> stringResponseEntity = HttpUtils.sendPostForm(url, headers, params, String.class);
        if (stringResponseEntity.getStatusCode().is2xxSuccessful()) {
            String filePath = stringResponseEntity.getBody();
            log.info("视频文件保存路径：{}", filePath);
        } else {
            log.error("视频文件保存失败，状态码：{}，原因：{}", stringResponseEntity.getStatusCodeValue(), stringResponseEntity.getBody());
        }
    }

}
