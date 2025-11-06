package com.tc.modules.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tc.modules.entity.TSysConfig;
import com.tc.modules.entity.TUser;
import com.tc.common.http.DataResponse;
import com.tc.common.utils.TokenUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Author jiangzhou
 * Date 2023/10/9
 * Description TODO
 **/
@Component
public class LoginService {

    /**
     * redis key 前缀
     */
    @Value("${spring.redis.prefix}")
    private String prefix;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.lock.prefix}")
    private String lockPrefix;

    @Value("${spring.lock.times}")
    private int lockTimes;

    @Value("${spring.lock.lockTime}")
    private int lockTime;

    @Value("${spring.lock.deviceControl}")
    private boolean deviceControl;

    @Resource
    private TUserService userService;

    @Resource
    private TSysConfigService sysConfigService;

    //生成token
    public String generateToken(String userId) {
        String token = TokenUtils.generate(userId);
        String id = prefix + userId;
        QueryWrapper<TSysConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("param_key", "loginTime");
        TSysConfig sysConfig = sysConfigService.getOne(queryWrapper);
        if (StringUtils.isEmpty(sysConfig.getParamValue()) || Integer.parseInt(sysConfig.getParamValue()) == 0) {
            redisTemplate.opsForValue().set(id, token);
        } else {
            redisTemplate.opsForValue().set(id, token, Integer.parseInt(sysConfig.getParamValue()), TimeUnit.MINUTES);
        }
        return token;
    }

    public int setLockTime(String userName) {
        int times = 1;
        if (redisTemplate.opsForValue().get(lockPrefix + userName) == null) {
            redisTemplate.opsForValue().set(lockPrefix + userName, "1", lockTime, TimeUnit.MINUTES);
        } else {
            times = Integer.parseInt(redisTemplate.opsForValue().get(lockPrefix + userName).toString()) + 1;
            redisTemplate.opsForValue().set(lockPrefix + userName, String.valueOf(times), lockTime, TimeUnit.MINUTES);
        }
        return lockTimes - times;
    }

    public int getLockedTime(String userName) {
        if (redisTemplate.opsForValue().get(lockPrefix + userName) != null && Integer.parseInt(redisTemplate.opsForValue().get(lockPrefix + userName).toString()) >= lockTimes) {
            return redisTemplate.getExpire(lockPrefix + userName, TimeUnit.SECONDS).intValue();
        }
        return 0;
    }

    public void removeLock(String userName) {
        redisTemplate.delete(lockPrefix + userName);
    }

    public Boolean isUsedDevice(TUser user, String ip) {
        if (!deviceControl) {
            return true;
        }
        if (user.getIpAddress() == null) {
            user.setIpAddress(ip);
            userService.updateById(user);
            return true;
        }
        return user.getIpAddress().equals(ip);
    }

    public DataResponse logout(String userId) {
        redisTemplate.delete(prefix + userId);
        return new DataResponse();
    }
}
