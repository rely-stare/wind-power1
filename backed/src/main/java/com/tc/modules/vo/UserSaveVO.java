package com.tc.modules.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Author jiangzhou
 * Date 2023/11/10
 * Description TODO
 **/
@Data
public class UserSaveVO {

    private Integer id;

    private String fullName;

    @NotEmpty(message = "用户名不可为空")
    private String userName;

    private String password;

    private String telephone;

    @NotNull(message = "角色不可为空")
    private Integer roleId;

    private Integer status;

    private String ipAddress;

}
