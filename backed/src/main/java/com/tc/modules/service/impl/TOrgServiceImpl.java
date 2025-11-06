package com.tc.modules.service.impl;


import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.modules.entity.TOrg;
import com.tc.modules.mapper.TOrgMapper;
import com.tc.modules.service.TOrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 组织架构+风机设备
 *
 * @author sunchao
 * @since 2024-03-04
 */
@Service
public class TOrgServiceImpl extends ServiceImpl<TOrgMapper, TOrg> implements TOrgService {

    @Autowired
    private TOrgMapper tOrgMapper;

    @Override
    public List getOrgTree(TOrg po) {
        TOrg torg = tOrgMapper.selectOne(new QueryWrapper<TOrg>().eq("id", po.getId()));
        List<TOrg> tOrgs = tOrgMapper.selectOrgListById(po);
        return TreeUtil.build(tOrgs, torg.getParentId().toString(), (treeNode, tree) -> {
            tree.setId(treeNode.getId().toString());
            tree.setName(treeNode.getOrgName());
            tree.setParentId(treeNode.getParentId().toString());
            tree.setWeight(treeNode.getId());
            tree.put("orgType", treeNode.getOrgType());
        });
    }
}
