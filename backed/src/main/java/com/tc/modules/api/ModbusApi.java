package com.tc.modules.api;

import com.tc.common.config.IotConfig;
import com.tc.common.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ModbusApi {

    @Autowired
    private IotConfig iotConfig;

    public void writeSingleRegisterBit(String deviceCode, int address, boolean value, int bitIndex) {
        try {
            String url = "http://" + iotConfig.getHost() + ":" + iotConfig.getPort() + "/modbus/writeSingleRegisterBit";
            Map<String, String> params = new HashMap<>();
            params.put("deviceCode", deviceCode);
            params.put("address", String.valueOf(address));
            params.put("bitIndex", String.valueOf(bitIndex));
            params.put("value", value ? "1" : "0");

            Map<String, String> headers = new HashMap<>();
            headers.put(iotConfig.getApiKey(), iotConfig.getApiSecret());

            ResponseEntity<String> stringResponseEntity = HttpUtils.sendPostForm(url, headers, params, String.class);
        } catch (Exception e) {
            log.error("writeSingleRegisterBit 请求失败", e);
        }
    }

    public void writeSingleRegisterReal(String deviceCode, int address, int value) {
        try {
            String url = "http://" + iotConfig.getHost() + ":" + iotConfig.getPort() + "/modbus/writeSingleRegisterReal";
            Map<String, String> params = new HashMap<>();
            params.put("deviceCode", deviceCode);
            params.put("address", String.valueOf(address));
            params.put("value", String.valueOf(value));

            Map<String, String> headers = new HashMap<>();
            headers.put(iotConfig.getApiKey(), iotConfig.getApiSecret());

            ResponseEntity<String> stringResponseEntity = HttpUtils.sendPostForm(url, headers, params, String.class);
        } catch (Exception e) {
            log.error("writeSingleRegisterReal 请求失败", e);
        }
    }

}
