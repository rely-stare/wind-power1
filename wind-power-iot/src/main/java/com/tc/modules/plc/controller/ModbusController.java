package com.tc.modules.plc.controller;

import com.tc.common.vo.Result;
import com.tc.modules.plc.entity.ModbusClientEntity;
import com.tc.modules.plc.pdu.ReadHoldingRegistersResponse;
import com.tc.modules.plc.pdu.WriteMultipleRegistersResponse;
import com.tc.modules.plc.pdu.WriteSingleRegisterResponse;
import com.tc.modules.plc.service.ModbusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/modbus")
public class ModbusController {

    @Autowired
    private ModbusService modbusService;

    @PostMapping("readSingleRegisterBit")
    public Result<?> readSingleRegisterBit(String deviceCode, int address, int bitIndex) throws Exception {
        boolean b = modbusService.ReadSingRegisterBit(deviceCode, address, bitIndex);
        return Result.ok(b);
    }

    @PostMapping("readSingRegisterReal")
    public Result<?> readSingRegisterReal(String deviceCode, int address) throws Exception {
        float real = modbusService.ReadSingRegisterReal(deviceCode, address);
        return Result.ok(real);
    }


    @PostMapping("readHoldingRegisters")
    public Result<?> ReadHoldingRegisters(String deviceCode, int address, int count) throws Exception {
        ReadHoldingRegistersResponse readHoldingRegistersResponse = modbusService.ReadHoldingRegisters(deviceCode, address, count);
        short[] registers = readHoldingRegistersResponse.getRegisters();
        return Result.ok(registers);
    }

    @PostMapping("writeSingleRegister")
    public Result<?> writeSingleRegister(String deviceCode, int address, int value) throws Exception {
        WriteSingleRegisterResponse writeSingleRegisterResponse = modbusService.writeSingleRegister(deviceCode, address, value);
        return Result.ok();
    }

    @PostMapping("writeSingleRegisterReal")
    public Result<?> writeSingleRegisterReal(String deviceCode, int address, float value) throws Exception {
        WriteMultipleRegistersResponse writeMultipleRegistersResponse = modbusService.writeSingleRegisterReal(deviceCode, address, value);
        return Result.ok();
    }

    @PostMapping("writeSingleRegisterBit")
    public Result<?> writeSingRegisterBit(String deviceCode, int address, boolean value, int bitIndex) throws Exception {
        WriteSingleRegisterResponse writeSingleRegisterResponse = modbusService.writeSingleRegisterBit(deviceCode, address, value, bitIndex);
        return Result.ok();
    }

    @PostMapping("connect")
    public Result<?> connect(ModbusClientEntity entity) throws Exception {
        modbusService.connect(entity);
        return Result.ok();
    }

    @PostMapping("disconnect")
    public Result<?> disconnect(String deviceCode) throws Exception {
        modbusService.disconnect(deviceCode);
        return Result.ok();
    }

    @PostMapping("test")
    public Result<?> tst(String deviceCode, int start, int end, float value) throws Exception {
        int i = 0;
        while (true) {
            if (start > end) {
                return Result.ok();
            }
            modbusService.writeSingleRegisterReal(deviceCode, start, value + i);
            start = start + 2;
            i++;
        }
    }
}
