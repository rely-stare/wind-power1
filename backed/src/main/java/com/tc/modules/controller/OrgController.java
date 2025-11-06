package com.tc.modules.controller;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.tc.common.annotation.Log;
import com.tc.common.enums.BusinessType;
import com.tc.modules.vo.OrgSaveVO;
import com.tc.common.http.DataResponse;
import com.tc.common.http.ErrorCode;
import com.tc.modules.entity.TOrg;
import com.tc.modules.entity.TSite;
import com.tc.modules.service.TOrgService;
import com.tc.modules.service.TSiteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组织架构+风机设备
 * @author sunchao
 * @since 2024-03-04
 */
@RestController
@Api(tags = "组织管理")
@RequestMapping("/org")
public class OrgController {

    @Resource
    private TOrgService orgService;

    @ApiOperation("组织管理查询")
    @GetMapping("/getOrgList")
    public DataResponse getOrgList(@RequestParam(required = false) String orgName) {
        QueryWrapper<TOrg> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", 0);
        queryWrapper.in("org_type", 1, 2);
        if (StringUtils.isNotEmpty(orgName)) {
            queryWrapper.like("org_name", orgName);
            List<TOrg> list = orgService.list(queryWrapper);
            Map<Integer, TOrg> resMap = new HashMap<>();
            list.stream().forEach(org -> {
                resMap.put(org.getId(), org);
                while (org.getParentId() != null && org.getParentId() != 0) {
                    TOrg parentOrg = orgService.getById(org.getParentId());
                    resMap.put(parentOrg.getId(), parentOrg);
                    org = parentOrg;
                }
            });
            list.stream().forEach(org -> {
                if (org.getOrgType() < 2) {
                    // 站点以上单位
                    QueryWrapper<TOrg> childWrapper = new QueryWrapper<>();
                    childWrapper.eq("parent_id", org.getId());
                    childWrapper.eq("is_delete", 0);
                    childWrapper.in("org_type", 1, 2);
                    List<TOrg> childOrgList = orgService.list(childWrapper);
                    getChildOrgList(childOrgList, resMap);
                }
            });
            return new DataResponse(resMap.values());
        } else {
            return new DataResponse(orgService.list(queryWrapper));
        }
    }

    public void getChildOrgList(List<TOrg> orgList, Map<Integer, TOrg> resMap) {
        orgList.stream().forEach(org -> {
            resMap.put(org.getId(), org);
            QueryWrapper<TOrg> childWrapper = new QueryWrapper<>();
            childWrapper.eq("parent_id", org.getId());
            childWrapper.eq("is_delete", 0);
            childWrapper.in("org_type", 1, 2);
            List<TOrg> childOrgList = orgService.list(childWrapper);
            if (CollectionUtil.isNotEmpty(childOrgList)) {
                this.getChildOrgList(childOrgList, resMap);
            }
        });
    }

    @ApiOperation("删除组织")
    @DeleteMapping("/delOrg")
    @Log(title = "组织管理", businessType = BusinessType.DELETE)
    public DataResponse delOrg(@RequestParam Integer orgId) {
        // 查询所有站点
        Map<Integer, TOrg> resMap = new HashMap<>();
        QueryWrapper<TOrg> childWrapper = new QueryWrapper<>();
        childWrapper.eq("parent_id", orgId);
        childWrapper.eq("is_delete", 0);
        childWrapper.in("org_type", 1, 2, 3, 4, 5);
        List<TOrg> childOrgList = orgService.list(childWrapper);
        getChildOrgList(childOrgList, resMap);
        if (CollectionUtil.isNotEmpty(resMap.values())) {
            Collection<TOrg> collection = resMap.values();
            for (TOrg org : collection) {
                if (org.getOrgType() == 2) {
                    return new DataResponse(ErrorCode.E3016, null);
                }
            }
        }
        delChild(orgId);
        return new DataResponse();
    }

    public void delChild(Integer orgId) {
        TOrg org = orgService.getById(orgId);
        org.setIsDelete(1);
        orgService.updateById(org);
        QueryWrapper<TOrg> childWrapper = new QueryWrapper<>();
        childWrapper.eq("parent_id", org.getId());
        childWrapper.eq("is_delete", 0);
        childWrapper.in("org_type", 1, 2, 3, 4, 5);
        List<TOrg> childList = orgService.list(childWrapper);
        if (CollectionUtil.isNotEmpty(childList)) {
            childList.stream().forEach(childOrg -> {
                this.delChild(childOrg.getId());
            });
        }
    }

    @ApiOperation("新增/修改组织")
    @PostMapping("/saveOrg")
    @Log(title = "组织管理", businessType = BusinessType.INSERT)
    public DataResponse saveOrg(@RequestBody @Validated OrgSaveVO orgSaveVO, @RequestHeader("userId") String userId) {
        // QueryWrapper<TOrg> orgQueryWrapper = new QueryWrapper<>();
        // orgQueryWrapper.eq("is_delete", 0);
        // orgQueryWrapper.eq("org_code", orgSaveVO.getOrgCode());
        // if (orgSaveVO.getId() != null) {
        //     orgQueryWrapper.ne("id", orgSaveVO.getId());
        // }
        // List<TOrg> orgList = orgService.list(orgQueryWrapper);
        // if (CollectionUtil.isNotEmpty(orgList)) {
        //     return new DataResponse(ErrorCode.E3014, null);
        // }
        TOrg org = new TOrg();
        TSite site = new TSite();
        BeanUtils.copyProperties(orgSaveVO, org);
        if (orgSaveVO.getId() == null) {
            org.setCreateBy(Integer.parseInt(userId));
            site.setCreateBy(Integer.parseInt(userId));
        }
        orgService.saveOrUpdate(org);
        if (orgSaveVO.getParentId() == null) {
            UpdateWrapper<TOrg> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", org.getId());
            updateWrapper.set("parent_id", null);
            orgService.update(updateWrapper);
        }
        return new DataResponse(org);
    }

    @ApiOperation("查询某一层级的组织")
    @GetMapping("/getOrgs")
    public DataResponse getOrgs(TOrg po) {
        QueryWrapper<TOrg> orgQueryWrapper = new QueryWrapper<>();
        orgQueryWrapper.eq("is_delete", 0);
        orgQueryWrapper.eq("org_type", po.getOrgType());
        orgQueryWrapper.eq(ObjectUtils.isNotEmpty(po.getParentId()),"parent_id", po.getParentId());
        List<TOrg> list = orgService.list(orgQueryWrapper);
        return new DataResponse(list);
    }

    @ApiOperation("风机设备树")
    @GetMapping("/tree.get")
    public DataResponse getOrgTree(TOrg po) {
        List list = orgService.getOrgTree(po);
        return new DataResponse(list);
    }
}
