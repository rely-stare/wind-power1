package com.tc.run;

import com.tc.modules.plc.service.ModbusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ModbusRunner implements ApplicationRunner {

    @Autowired
    private ModbusService modbusService;

    @Override
    public void run(ApplicationArguments args) {
        modbusService.init();
    }
}
