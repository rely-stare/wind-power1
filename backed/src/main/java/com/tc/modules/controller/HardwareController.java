package com.tc.modules.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tc.common.core.controller.BaseController;
import com.tc.common.http.DataResponse;
import com.tc.modules.entity.TDictionary;
import com.tc.modules.entity.THardware;
import com.tc.modules.entity.THardwareTemplate;
import com.tc.modules.service.TCameraInfoService;
import com.tc.modules.service.TDictionaryService;
import com.tc.modules.service.THardwareService;
import com.tc.modules.service.THardwareTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "硬件管理")
@RestController
@RequestMapping("/hardware")
public class HardwareController extends BaseController {

    @Autowired
    private THardwareService hardwareService;

    @Autowired
    private TDictionaryService dictionaryService;

    @Autowired
    private TCameraInfoService cameraInfoService;

    @Autowired
    private THardwareTemplateService hardwareTemplateService;

    @GetMapping("/list")
    @ApiOperation(value = "获取硬件列表")
    public DataResponse<List<THardware>> getHardwareList(THardware po) {
        LambdaQueryWrapper<THardware> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(THardware::getSiteId, po.getSiteId());
        queryWrapper.eq(THardware::getIsShow, 1);
        List<THardware> list = hardwareService.list(queryWrapper);
        return DataResponse.success(list);
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加硬件")
    public DataResponse<?> addHardware(@RequestBody THardware po, @RequestHeader("userId") String userId) {
        po.setCreateBy(userId);
        po.setCreateTime(new Date());
        hardwareService.save(po);
        return DataResponse.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新硬件")
    public DataResponse<?> updateHardware(@RequestBody THardware po, @RequestHeader("userId") String userId) {
        hardwareService.updateById(po);
        return DataResponse.success();
    }

    @DeleteMapping("/delete")
    @ApiOperation(value = "删除硬件")
    public DataResponse<?> deleteHardware(@RequestParam Integer id) {
        hardwareService.removeById(id);
        return new DataResponse();
    }


    @GetMapping("/getHardwareTree")
    @ApiOperation(value = "获取硬件树形结构")
    public DataResponse<?> getHardwareTree(@RequestParam Integer siteId) {
        List<THardware> list = hardwareService.list(new LambdaQueryWrapper<THardware>()
                .eq(THardware::getSiteId, siteId)
        );

        list.forEach(item -> item.setLabel(item.getMonitorLocation()));

        Map<Integer, List<THardware>> collect = list.stream().collect(Collectors.groupingBy(THardware::getHardwareType));
        LambdaQueryWrapper<TDictionary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TDictionary::getType, "hardwareType");
        List<TDictionary> dictionaryList = dictionaryService.list(queryWrapper);

        List<Map<String, Object>> res = new ArrayList<>();
        for (Integer hardwareType : collect.keySet()) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("id", hardwareType);
            temp.put("label", dictionaryList.stream().filter(s -> s.getParamValue().equals(hardwareType.toString())).findFirst().get().getParamKey());
            temp.put("list", collect.get(hardwareType));
            res.add(temp);
        }
        return DataResponse.success(res);
    }

    @GetMapping("/getHardwareTreeTemp")
    @ApiOperation(value = "获取硬件树形结构模板")
    public DataResponse<?> getHardwareTreeTemp() {
        List<THardwareTemplate> list = hardwareTemplateService.list();
        list.forEach(item -> item.setLabel(item.getMonitorLocation()));

        Map<Integer, List<THardwareTemplate>> collect = list.stream().collect(Collectors.groupingBy(THardwareTemplate::getHardwareType));
        LambdaQueryWrapper<TDictionary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TDictionary::getType, "hardwareType");
        List<TDictionary> dictionaryList = dictionaryService.list(queryWrapper);

        List<Map<String, Object>> res = new ArrayList<>();
        for (Integer hardwareType : collect.keySet()) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("id", hardwareType);
            temp.put("label", dictionaryList.stream().filter(s -> s.getParamValue().equals(hardwareType.toString())).findFirst().get().getParamKey());
            temp.put("list", collect.get(hardwareType));
            res.add(temp);
        }
        return DataResponse.success(res);
    }

}
