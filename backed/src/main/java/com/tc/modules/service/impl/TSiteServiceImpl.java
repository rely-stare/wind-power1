package com.tc.modules.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.common.redis.RedisCache;
import com.tc.modules.entity.TSite;
import com.tc.modules.mapper.TSiteMapper;
import com.tc.modules.service.TSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 站点表 服务实现类
 * </p>
 *
 * @author Jz
 * @since 2023-12-11
 */
@Service
public class TSiteServiceImpl extends ServiceImpl<TSiteMapper, TSite> implements TSiteService {

    @Autowired
    private RedisCache redisCache;

    private static final String SITE_STATUS_PREFIX = "iot:device-status:site:heartBeat:";
    private static final String TURBINE_STATUS_PREFIX = "iot:device-status:site:status:";

    @Override
    public boolean getSiteStatus(Integer siteId) {
        Boolean cacheObject = redisCache.getCacheObject(SITE_STATUS_PREFIX + siteId);
        return cacheObject != null;
    }


    public int getTurbineStatus(Integer siteId) {
        Boolean cacheObject = redisCache.getCacheObject(TURBINE_STATUS_PREFIX + siteId);
        if (cacheObject == null) {
            return 99;
        } else if (cacheObject) {
            return 1;
        } else {
            return 0;
        }
    }
}
