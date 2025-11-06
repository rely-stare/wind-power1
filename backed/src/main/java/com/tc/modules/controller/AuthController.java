package com.tc.modules.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.tc.modules.entity.TSysConfig;
import com.tc.modules.entity.TUser;
import com.tc.modules.service.LoginService;
import com.tc.modules.service.TSysConfigService;
import com.tc.modules.service.TUserService;
import com.tc.common.CaptchaGenerator;
import com.tc.modules.vo.LoginRequest;
import com.tc.common.http.DataResponse;
import com.tc.common.http.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Author jiangzhou
 * Date 2023/10/9
 * Description 登录
 **/
@RestController
@Api(tags = "登录")
@RequestMapping("/auth")
public class AuthController {

    private final String validatePrefix = "validate:";
    @Resource
    private TUserService userService;
    @Resource
    private LoginService loginService;
    @Resource
    private TSysConfigService sysConfigService;
    @Resource
    private CaptchaGenerator captchaGenerator;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @ApiOperation("登录")
    @PostMapping("/login")
    public DataResponse login(@RequestBody @Validated LoginRequest loginRequest, @RequestHeader("ip") String ip) {
        // 判断是否已锁定 redis
        int lockTime = loginService.getLockedTime(loginRequest.getUsername());
        if (lockTime != 0) {
            return new DataResponse(ErrorCode.E3003, lockTime);
        }

        QueryWrapper<TSysConfig> sysWrapper = new QueryWrapper<>();
        sysWrapper.eq("param_key", "verifySwitch");
        TSysConfig sysConfig = sysConfigService.getOne(sysWrapper);
        if (sysConfig.getParamValue().equals("1")) {
            // 验证码校验失败
            String captcha = redisTemplate.opsForValue().get(validatePrefix + ip);
            if (captcha == null) {
                // 验证码已过期
                return new DataResponse(ErrorCode.E3008, null);
            }
            if (!loginRequest.getCaptcha().equals(captcha)) {
                // 验证码不匹配
                return new DataResponse(ErrorCode.E3009, null);
            }
        }

        // 根据用户名密码是否匹配用户
        QueryWrapper<TUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", loginRequest.getUsername()).eq("status", 1).eq("is_delete", 0);
        TUser user = userService.getOne(queryWrapper);
        if (user == null) {
            return new DataResponse(ErrorCode.E3013, loginService.setLockTime(loginRequest.getUsername()));
        }
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return new DataResponse(ErrorCode.E3000, loginService.setLockTime(loginRequest.getUsername()));
        }

        // 判断是否常用设备
        if (!loginService.isUsedDevice(user, ip)) {
            return new DataResponse(ErrorCode.E3004, null);
        }

        // 解除登录锁定
        loginService.removeLock(loginRequest.getUsername());

        // 生成token
        String token = loginService.generateToken(user.getId().toString());

        // 更新登录时间
        user.setLastLoginTime(new Date());
        userService.updateById(user);

        // 密码是否已过期
        if (user.getPasswordInvalidTime().before(new Date())) {
            return new DataResponse(ErrorCode.E2001, token);
        }

        return new DataResponse(token);
    }

    @ApiOperation("登出")
    @GetMapping("/logout")
    public DataResponse logout(@RequestHeader("userId") String userId) {
        return loginService.logout(userId);
    }

    @ApiOperation("生成验证码")
    @GetMapping("/captcha")
    public DataResponse captcha(@RequestHeader("ip") String ip) {
        return new DataResponse(captchaGenerator.generateCaptchaImage(ip));
    }
}
