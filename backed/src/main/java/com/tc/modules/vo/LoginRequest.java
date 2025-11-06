package com.tc.modules.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * Author jiangzhou
 * Date 2023/10/9
 * Description 登录请求
 **/
@Data
public class LoginRequest {

    @NotEmpty(message = "用户名不可为空")
    private String username;

    @NotEmpty(message = "密码不可为空")
    private String password;

    // 验证码
    private String captcha;
}
