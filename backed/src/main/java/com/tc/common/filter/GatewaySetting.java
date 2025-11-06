package com.tc.common.filter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Author jiangzhou
 * Date 2023/9/26
 * Description TODO
 **/
@Data
@ConfigurationProperties("setting")
@Component
public class GatewaySetting {

    private List<String> whiteUrls;

}
