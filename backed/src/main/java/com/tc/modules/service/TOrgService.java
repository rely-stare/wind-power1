package com.tc.modules.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tc.modules.entity.TOrg;

import java.util.List;

/**
 * 组织架构+风机设备
 * @author sunchao
 * @since 2024-03-04
 */
public interface TOrgService extends IService<TOrg> {

    List getOrgTree(TOrg po);
}
