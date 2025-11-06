package com.tc.modules.vo;

import lombok.Data;

import java.util.List;

/**
 * Author jiangzhou
 * Date 2023/11/9
 * Description TODO
 **/
@Data
public class RoleListVo {

    // 角色id
    private Integer id;

    // 角色名
    private String roleName;

    // 任务控制权限
    private Integer taskControl;

    // 备注
    private String remark;

    // 关联用户数
    private Long number;

    // 角色类型
    private Integer roleType;

    // 关联的菜单ids
    private List<Integer> menuIds;
}
