package com.tc.modules.message.sse.controller;

import com.tc.modules.message.sse.service.SseService;
import com.tc.modules.service.TCameraInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Api(tags = "sse 消息")
@RestController
@RequestMapping("/sse")
public class SseController {

    @Autowired
    private SseService service;

    @Autowired
    private TCameraInfoService cameraInfoService;

    @ApiOperation(value = "订阅超速数据")
    @GetMapping("/speed/{userid}/{siteId}")
    public SseEmitter speedSubscribeToDevice(@PathVariable String userid, @PathVariable String siteId) {
        return service.getConnSpeed(siteId, userid);
    }

    @ApiOperation(value = "订阅转速稳定性数据")
    @GetMapping("/speedFluctuate/{userid}/{siteId}")
    public SseEmitter speedFluctuate(@PathVariable String userid, @PathVariable String siteId) {
        return service.getSpeedFluctuateConn(siteId, userid);
    }

    @ApiOperation(value = "订阅转速震荡数据")
    @GetMapping("/speedAverage/{userid}/{siteId}")
    public SseEmitter speedAverage(@PathVariable String userid, @PathVariable String siteId) {
        return service.getConnAverage(siteId, userid);
    }

    @ApiOperation(value = "订阅temperature数据")
    @GetMapping("/temperature/{userid}/{hardwareId}")
    public SseEmitter temperatureSubscribeToDevice(@PathVariable String userid, @PathVariable String hardwareId) {
        return service.getConnTemperature(hardwareId, userid);
    }

    // 关闭设备连接
    @ApiOperation(value = "关闭 speed 设备连接")
    @DeleteMapping("/speed/{userid}/{hardwareId}")
    public ResponseEntity<String> closeConnection(@PathVariable String hardwareId, @PathVariable String userid) {
        service.closeConnSpeed(hardwareId, userid);
        return ResponseEntity.ok("Connection closed");
    }



}
