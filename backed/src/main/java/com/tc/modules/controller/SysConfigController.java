package com.tc.modules.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import com.tc.modules.entity.TSysConfig;
import com.tc.modules.service.TSysConfigService;
import com.tc.common.http.DataResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Jz
 * @since 2023-11-03
 */
@RestController
@Api(tags = "系统配置")
@RequestMapping("/config")
public class SysConfigController {

    @Resource
    private TSysConfigService sysConfigService;

    @ApiOperation("查询系统配置")
    @GetMapping("/getConfig")
    public DataResponse getConfig() {
        QueryWrapper<TSysConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", "login");
        List<TSysConfig> list = sysConfigService.list(queryWrapper);
        Map<String, String> res = new HashMap<>();
        list.stream().forEach(sysConfig -> res.put(sysConfig.getParamKey(), sysConfig.getParamValue()));
        return new DataResponse(res);
    }

    @ApiOperation("查询所有配置")
    @GetMapping("/getAllConfig")
    public DataResponse getAllConfig() {
        List<TSysConfig> list = sysConfigService.list();
        Map<String, String> res = new HashMap<>();
        list.stream().forEach(sysConfig -> res.put(sysConfig.getParamKey(), sysConfig.getParamValue()));
        return new DataResponse(res);
    }

    @ApiOperation("修改系统配置")
    @PostMapping("saveConfig")
    public DataResponse saveConfig(@RequestBody Map<String, String> configMap) {
        if (CollectionUtils.isEmpty(configMap)) {
            return new DataResponse();
        }
        configMap.entrySet().stream().forEach(entry -> {
            UpdateWrapper<TSysConfig> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("param_key", entry.getKey()).set("param_value", entry.getValue());
            sysConfigService.update(updateWrapper);
        });
        return new DataResponse();
    }
}
