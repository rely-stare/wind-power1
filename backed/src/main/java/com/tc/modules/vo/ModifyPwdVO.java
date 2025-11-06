package com.tc.modules.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * Author jiangzhou
 * Date 2023/11/10
 * Description TODO
 **/
@Data
public class ModifyPwdVO {

    @NotEmpty(message = "原密码不可为空")
    private String historyPassword;

    @NotEmpty(message = "新密码不可为空")
    private String newPassword;
}
