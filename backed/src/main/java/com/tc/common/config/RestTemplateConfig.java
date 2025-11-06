package com.tc.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

/**
 * @author sunchao
 * @date 2024/3/18 15:19
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    @Bean
    HttpHeaders getHttpHeaders(){
        return new HttpHeaders();
    }
}
