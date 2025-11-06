package com.tc.modules.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.tc.modules.entity.TSysConfig;
import com.tc.modules.entity.TUser;
import com.tc.modules.service.TSysConfigService;
import com.tc.modules.service.TUserService;
import com.tc.modules.vo.ModifyPwdVO;
import com.tc.modules.vo.UserQueryVO;
import com.tc.modules.vo.UserSaveVO;
import com.tc.modules.vo.UserVO;
import com.tc.common.annotation.Log;
import com.tc.common.enums.BusinessType;
import com.tc.common.http.DataResponse;
import com.tc.common.http.ErrorCode;
import com.tc.common.utils.CryptoUtils;
import com.tc.common.utils.DateUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author jiangzhou
 * Date 2023/11/3
 * Description TODO
 **/
@RestController
@Api(tags = "用户管理")
@RequestMapping("/user")
public class UserController {

    private final String secretKey = "AyYkmAVZ6XsUes0ICjq5TA==";
    @Resource
    private TUserService userService;
    @Resource
    private TSysConfigService sysConfigService;

    @ApiOperation("获取登录者信息")
    @GetMapping("/getUserInfo")
    public DataResponse getUserInfo(@RequestHeader("userId") String userId) {
        QueryWrapper<TUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "full_Name", "user_name", "telephone", "role_id", "last_login_time").eq("id", userId);
        TUser user = userService.getOne(queryWrapper);
        if (user == null) {
            return new DataResponse(ErrorCode.E3006, null);
        } else {
            UserVO userRes = new UserVO();
            BeanUtils.copyProperties(user, userRes);
            return new DataResponse(userRes);
        }
    }

    @ApiOperation("获取用户管理列表")
    @PostMapping("/getUserList")
    public DataResponse getUserList(@RequestBody @Validated UserQueryVO userQueryVO) {
        QueryWrapper<TUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", 0);
        if (userQueryVO.getStatus() != null) {
            queryWrapper.eq("status", userQueryVO.getStatus());
        }
        if (userQueryVO.getRoleId() != null) {
            queryWrapper.eq("role_id", userQueryVO.getRoleId());
        }
        if (StringUtils.isNotEmpty(userQueryVO.getUserName())) {
            queryWrapper.like("user_name", userQueryVO.getUserName());
        }
        if (StringUtils.isNotEmpty(userQueryVO.getFullName())) {
            queryWrapper.like("full_name", userQueryVO.getFullName());
        }
        if (StringUtils.isNotEmpty(userQueryVO.getTelephone())) {
            queryWrapper.like("telephone", userQueryVO.getTelephone());
        }

        Map<String, Object> res = new HashMap<>();
        Page<TUser> pageInfo = new Page<>(userQueryVO.getPage(), userQueryVO.getSize());
        IPage<TUser> userPage = userService.page(pageInfo, queryWrapper);
        res.put("total", userPage.getTotal());
        res.put("list", userPage.getRecords());
        return new DataResponse(res);
    }

    @ApiOperation("删除用户")
    @GetMapping("/delUser")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    public DataResponse delUser(@RequestParam Integer[] userIds) {
        QueryWrapper<TUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", userIds);
        List<TUser> userList = userService.list(queryWrapper);
        userList.stream().forEach(user -> {
            user.setIsDelete(1);
        });
        return new DataResponse(userService.updateBatchById(userList));
    }

    @ApiOperation("重置用户")
    @GetMapping("/resetUser")
    public DataResponse resetUser(@RequestParam Integer userId) throws Exception {
        TUser user = userService.getById(userId);
        // 重置密码
        QueryWrapper<TSysConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("param_key", "defaultPassword");
        TSysConfig sysConfig = sysConfigService.getOne(queryWrapper);
        user.setPassword(CryptoUtils.encryptSymmetrically(secretKey, null, sysConfig.getParamValue(), CryptoUtils.Algorithm.Encryption.AES_ECB_PKCS5));
        // 重置密码失效时间
        QueryWrapper<TSysConfig> sysWrapper = new QueryWrapper<>();
        sysWrapper.eq("param_key", "pwdRememberTime");
        TSysConfig config = sysConfigService.getOne(sysWrapper);
        user.setPasswordInvalidTime(DateUtils.addDays(new Date(), Integer.parseInt(config.getParamValue())));
        return new DataResponse(userService.updateById(user));
    }
//    修改密码测试类
//    public static void main(String[] args) throws Exception {
//        System.out.println(CryptoUtils.encryptSymmetrically("AyYkmAVZ6XsUes0ICjq5TA==", null, "admin", CryptoUtils.Algorithm.Encryption.AES_ECB_PKCS5));
//    }
    @ApiOperation("新增/修改用户")
    @PostMapping("/saveUser")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    public DataResponse saveUser(@RequestBody @Validated UserSaveVO userSaveVO, @RequestHeader("userId") String userId) throws Exception {
        if (!validateUserName(userSaveVO)) {
            // 用户名重复
            return new DataResponse(ErrorCode.E3010, null);
        }
        TUser user = new TUser();
        BeanUtils.copyProperties(userSaveVO, user);
        if (user.getId() == null) {
            QueryWrapper<TSysConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("param_key", "pwdRememberTime");
            TSysConfig sysConfig = sysConfigService.getOne(queryWrapper);
            user.setPassword(CryptoUtils.encryptSymmetrically(secretKey, null, userSaveVO.getPassword(), CryptoUtils.Algorithm.Encryption.AES_ECB_PKCS5));
            user.setPasswordInvalidTime(DateUtils.addDays(new Date(), Integer.parseInt(sysConfig.getParamValue())));
            user.setCreateBy(Integer.parseInt(userId));
        }
        return new DataResponse(userService.saveOrUpdate(user));
    }

    // 校验用户名重复
    public boolean validateUserName(UserSaveVO userSaveVO) {
        QueryWrapper<TUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", 0);
        if (userSaveVO.getId() == null) {
            // 新增用户
            queryWrapper.eq("user_name", userSaveVO.getUserName());
        } else {
            // 编辑用户
            queryWrapper.eq("user_name", userSaveVO.getUserName());
            queryWrapper.ne("id", userSaveVO.getId());
        }
        return userService.getOne(queryWrapper) == null;
    }

    @ApiOperation("修改密码")
    @PostMapping("/modifyPassword")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    public DataResponse modifyPassword(@RequestBody @Validated ModifyPwdVO modifyPwdVO, @RequestHeader("userId") String userId) {
        TUser user = userService.getById(userId);
        if (!modifyPwdVO.getHistoryPassword().equals(user.getPassword())) {
            return new DataResponse(ErrorCode.E3011, null);
        }
        user.setPassword(modifyPwdVO.getNewPassword());
        // 重置密码失效时间
        QueryWrapper<TSysConfig> sysWrapper = new QueryWrapper<>();
        sysWrapper.eq("param_key", "pwdRememberTime");
        TSysConfig config = sysConfigService.getOne(sysWrapper);
        user.setPasswordInvalidTime(DateUtils.addDays(new Date(), Integer.parseInt(config.getParamValue())));
        return new DataResponse(userService.updateById(user));
    }

    public static void main(String[] args) throws Exception {
        System.out.println(CryptoUtils.encryptSymmetrically("AyYkmAVZ6XsUes0ICjq5TA==", null, "admin", CryptoUtils.Algorithm.Encryption.AES_ECB_PKCS5));
    }
}
