package com.tc.modules.vo;

import lombok.Data;

import java.util.Date;

/**
 * Author jiangzhou
 * Date 2023/11/7
 * Description TODO
 **/
@Data
public class UserVO {

    private Integer id;

    private String fullName;

    private String userName;

    private String telephone;

    private Integer roleId;

    private Date lastLoginTime;
}
