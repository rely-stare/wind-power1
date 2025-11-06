package com.tc.common.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "iot")
@Data
public class IotConfig {
    /**
     * iot地址
     */
    private String host;

    /**
     * iot端口
     */
    private int port;

    /**
     * iot apiKey
     */
    private String apiKey;

    /**
     * iot apiSecret
     */
    private String apiSecret;
}
