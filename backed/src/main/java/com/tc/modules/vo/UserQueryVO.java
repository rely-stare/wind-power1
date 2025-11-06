package com.tc.modules.vo;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Author jiangzhou
 * Date 2023/11/3
 * Description TODO
 **/
@Data
public class UserQueryVO {

    // 用户名
    private String userName;

    // 姓名
    private String fullName;

    // 手机号
    private String telephone;

    // 状态 1-可用 0-禁用
    private Integer status;

    // 角色id
    private Integer roleId;

    @NotNull(message = "页码为空")
    private Integer page;

    @NotNull(message = "条数为空")
    @Min(value = 1, message = "最少查询1条")
    @Max(value = 100, message = "最多查询100条")
    private Integer size;
}
