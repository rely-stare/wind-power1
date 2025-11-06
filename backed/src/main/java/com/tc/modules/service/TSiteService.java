package com.tc.modules.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.tc.modules.entity.TSite;

/**
 * <p>
 * 站点表 服务类
 * </p>
 *
 * @author Jz
 * @since 2023-12-11
 */
public interface TSiteService extends IService<TSite> {

    public boolean getSiteStatus(Integer siteId);

}
