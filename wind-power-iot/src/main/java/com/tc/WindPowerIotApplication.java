package com.tc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.tc.modules.**.mapper")
public class WindPowerIotApplication {

	public static void main(String[] args) {
		SpringApplication.run(WindPowerIotApplication.class, args);
	}

}
