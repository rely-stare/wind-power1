package com.tc.modules.controller;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tc.common.http.DataResponse;
import com.tc.modules.api.ModbusApi;
import com.tc.modules.entity.*;
import com.tc.modules.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/config/center")
@Api(tags = "配置管理")
public class ConfigCenterController {

    @Autowired
    private TConfigCenterService configCenterService;

    @Autowired
    private TConfigRuleService configRuleService;

    @Autowired
    private ModbusApi modbusApi;

    @Autowired
    private TModbusProtocolService modbusProtocolService;

    @GetMapping("/list")
    @ApiOperation(value = "获取配置中心列表")
    public DataResponse<List<TConfigCenter>> getSiteConfigList(Integer menuCode) {
        LambdaQueryWrapper<TConfigCenter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TConfigCenter::getMenuCode, menuCode);
        List<TConfigCenter> list = configCenterService.list(queryWrapper);
        return  DataResponse.success(list);
    }


    @GetMapping("/getRule")
    @ApiOperation(value = "根据菜单编码获取规则列表")
    public DataResponse<List<TConfigRule>> getConfigRuleList(Integer menuCode) {
        LambdaQueryWrapper<TConfigRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TConfigRule::getMenuCode, menuCode);
        List<TConfigRule> list = configRuleService.list(queryWrapper);
        return  DataResponse.success(list);
    }

    @GetMapping("/getTree")
    @ApiOperation(value = "获取配置中心树形结构")
    public DataResponse<List<Tree<Integer>>> getMenuTree() {
        List<TConfigCenter> list = configCenterService.list();
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        treeNodeConfig.setIdKey("id");
        treeNodeConfig.setParentIdKey("parentId");
        treeNodeConfig.setChildrenKey("children");
        treeNodeConfig.setNameKey("menuName");
        List<Tree<Integer>> menuTree = TreeUtil.build(list, 0, treeNodeConfig, (treeNode, tree) -> {
            tree.setId(treeNode.getId());
            tree.setParentId(treeNode.getParentId());
            tree.setName(treeNode.getMenuName());
            tree.putExtra("menuCode", treeNode.getMenuCode());
            tree.putExtra("menuType", treeNode.getMenuType());
        });
        return DataResponse.success(menuTree);
    }

    @PostMapping("/edit")
    @ApiOperation(value = "编辑配置规则")
    public DataResponse<?> edit(@RequestBody TConfigRule rule) {
        TConfigRule configRule = configRuleService.getById(rule.getId());
        configRule.setSymbol(rule.getSymbol());
        configRule.setThreshold(rule.getThreshold());
        configRule.setUnit(rule.getUnit());
        configRuleService.updateById(configRule);
        return DataResponse.success();
    }

    @PostMapping("syncConfigToModbus")
    @ApiOperation(value = "同步配置到modbus")
    public DataResponse<?> syncConfigToModbus(@RequestBody String siteId) {

        List<TConfigRule> list = configRuleService.list();
        List<TConfigRule> configRules = list.stream().filter(s -> StringUtils.isNotBlank(s.getThreshold()) && StringUtils.isNotBlank(s.getModbusField())).collect(Collectors.toList());
        List<TModbusProtocol> modbusProtocols = modbusProtocolService.list();

        for (TConfigRule configRule : configRules) {
            modbusProtocols.stream()
                    .filter(item -> configRule.getModbusField().equalsIgnoreCase(item.getField()))
                    .findFirst().ifPresent(item -> {
                        modbusApi.writeSingleRegisterReal(siteId, item.getAddress(), Integer.parseInt(configRule.getThreshold()));
                    });
        }
        return DataResponse.success();
    }
}
