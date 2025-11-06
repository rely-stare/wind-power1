package com.tc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动
 * @author sunchao
 */
@EnableScheduling
@SpringBootApplication
public class WindPowerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WindPowerApplication.class, args);
    }

}
