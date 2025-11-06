package com.tc.modules.vo;

import lombok.Data;

/**
 * Author jiangzhou
 * Date 2023/11/7
 * Description TODO
 **/
@Data
public class RoleVo {

    // 角色id
    private Integer id;

    // 角色名
    private String roleName;

    // 任务控制权限
    private Integer taskControl;

    // 备注
    private String remark;

    // 菜单ids
    private Integer[] menuIds;

}
