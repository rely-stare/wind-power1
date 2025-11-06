package com.tc.modules.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tc.common.annotation.Log;
import com.tc.common.enums.BusinessType;
import com.tc.common.http.DataResponse;
import com.tc.common.http.ErrorCode;

import com.tc.common.utils.ServletUtils;
import com.tc.modules.entity.*;
import com.tc.modules.service.*;
import com.tc.modules.vo.SiteQueryVO;
import com.tc.modules.vo.SiteSaveVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 组织管理
 *
 * @author yinxy
 */
@RestController
@Api(tags = "风电机组")
@RequestMapping("site")
public class SiteController {

    @Resource
    private TOrgService orgService;

    @Resource
    private TSiteService siteService;

    @Resource
    private THardwareService hardwareService;

    @Resource
    private THardwareTemplateService hardwareTemplateService;

    @ApiOperation("查询机组列表")
    @PostMapping("/getSiteList")
    public DataResponse<?> getSiteList(@RequestBody @Validated SiteQueryVO siteQueryVO) {
        LambdaQueryWrapper<TSite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(siteQueryVO.getSiteName()), TSite::getSiteName, siteQueryVO.getSiteName());
        queryWrapper.in(siteQueryVO.getOrgIds().length > 0, TSite::getOrgId, Arrays.asList(siteQueryVO.getOrgIds()));

        Page<TSite> page = new Page<>(siteQueryVO.getPage(), siteQueryVO.getSize());
        IPage<TSite> pageList = siteService.page(page, queryWrapper);
        List<Integer> orgIdList = pageList.getRecords().stream().map(TSite::getOrgId).distinct().collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(orgIdList)) {
            LambdaQueryWrapper<TOrg> orgQueryWrapper = new LambdaQueryWrapper<>();
            orgQueryWrapper.in(TOrg::getId, orgIdList);
            List<TOrg> orgList = orgService.list(orgQueryWrapper);
            pageList.getRecords().forEach(site -> {
                site.setOrgName(orgList.stream().filter(o -> o.getId().equals(site.getOrgId())).findFirst().get().getOrgName());
            });
        }
        return DataResponse.success(pageList);
    }


    @ApiOperation("查询单个机组")
    @GetMapping("/getSite")
    public DataResponse<TSite> getSite(@RequestParam Integer id) {
        TSite site = siteService.getById(id);
        if (site != null) {
            QueryWrapper<TOrg> orgQueryWrapper = new QueryWrapper<>();
            orgQueryWrapper.eq("id", site.getOrgId());
            TOrg org = orgService.getOne(orgQueryWrapper);
            site.setOrgName(org.getOrgName());
        }
        return DataResponse.success(site);
    }


    @ApiOperation("新增/修改机组")
    @PostMapping("/saveSite")
    @Log(title = "组织管理", businessType = BusinessType.INSERT)
    public DataResponse<?> saveSite(@RequestBody SiteSaveVO siteSaveVO, @RequestHeader("userId") String userId) {
        TSite site = new TSite();
        BeanUtils.copyProperties(siteSaveVO, site);
        site.setCreateBy(Integer.parseInt(userId));
        siteService.saveOrUpdate(site);

        if (siteSaveVO.getId() == null) {
            List<THardwareTemplate> list = hardwareTemplateService.list();
            for (THardwareTemplate template : list) {
                THardware hardware = new THardware();
                hardware.setHardwareType(template.getHardwareType());
                hardware.setMonitorLocation(template.getMonitorLocation());
                hardware.setSiteId(site.getId());
                hardware.setIsShow(template.getHardwareType() == 1 ? 0 : 1);
                hardware.setMonitorType(template.getMonitorType());
                hardware.setCreateBy(userId);
                hardware.setCreateTime(new Date());
                hardwareService.save(hardware);
            }
        }
        return DataResponse.success();
    }

    @ApiOperation("查询所有机组")
    @GetMapping("/getAllSite")
    public DataResponse<List<TSite>> getAllSite() {
        QueryWrapper<TSite> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("is_delete", 0);
        return DataResponse.success(siteService.list(queryWrapper));
    }

    @ApiOperation("删除机组")
    @DeleteMapping("/delSite")
    public DataResponse<?> deleteSite(Integer id) {
        siteService.removeById(id);
        return DataResponse.success();
    }

    @GetMapping("/getSiteListByOrgId")
    @ApiOperation("根据组织ID查询机组")
    public DataResponse<List<TSite>> getSiteListByOrgId(String orgId) {
        LambdaQueryWrapper<TSite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TSite::getOrgId, orgId);
        List<TSite> list = siteService.list(queryWrapper);
        return DataResponse.success(list);
    }
}
