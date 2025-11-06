package com.tc.modules.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tc.modules.entity.TOrg;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 组织架构+风机设备
 * @author sunchao
 * @since 2024-03-04
 */
@Mapper
public interface TOrgMapper extends BaseMapper<TOrg> {

    List<TOrg> selectOrgListById(TOrg po);
}
