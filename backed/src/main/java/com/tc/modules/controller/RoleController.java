package com.tc.modules.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.tc.common.annotation.Log;
import com.tc.common.enums.BusinessType;
import com.tc.modules.entity.TRole;
import com.tc.modules.entity.TRoleMenu;
import com.tc.modules.entity.TUser;
import com.tc.modules.service.TRoleMenuService;
import com.tc.modules.service.TRoleService;
import com.tc.modules.service.TUserService;
import com.tc.modules.vo.RoleListVo;
import com.tc.modules.vo.RoleQueryVO;
import com.tc.modules.vo.RoleVo;
import com.tc.common.http.DataResponse;
import com.tc.common.http.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Jz
 * @since 2023-11-06
 */
@RestController
@Api(tags = "角色管理")
@RequestMapping("/role")
public class RoleController {

    @Resource
    private TRoleService roleService;

    @Resource
    private TUserService userService;

    @Resource
    private TRoleMenuService roleMenuService;

    @ApiOperation("查询角色列表")
    @PostMapping("/getRoleList")
    public DataResponse getRoleList(@RequestBody RoleQueryVO roleQueryVO) {
        QueryWrapper<TRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("is_delete", 0);
        if (!StringUtils.isBlank(roleQueryVO.getRoleName())) {
            queryWrapper.like("role_name", roleQueryVO.getRoleName());
        }
        if (roleQueryVO.getPage() == null || roleQueryVO.getSize() == null) {
            return new DataResponse(roleService.list(queryWrapper));
        }

        Map<String, Object> res = new HashMap<>();
        Page<TRole> pageInfo = new Page<>(roleQueryVO.getPage(), roleQueryVO.getSize());
        IPage<TRole> rolePage = roleService.page(pageInfo, queryWrapper);
        List<TRole> list = rolePage.getRecords();
        List<RoleListVo> resList = new ArrayList<>();
        list.stream().forEach(role -> {
            RoleListVo roleRes = new RoleListVo();
            BeanUtils.copyProperties(role, roleRes);
            QueryWrapper<TUser> userWrapper = new QueryWrapper<>();
            userWrapper.eq("role_id", role.getId());
            roleRes.setNumber(userService.count(userWrapper));
            QueryWrapper<TRoleMenu> roleMenuQueryWrapper = new QueryWrapper<>();
            roleMenuQueryWrapper.eq("role_id", role.getId());
            List<TRoleMenu> roleMenuList = roleMenuService.list(roleMenuQueryWrapper);
            List<Integer> menuIds = new ArrayList<>();
            roleMenuList.stream().forEach(roleMenu -> {
                menuIds.add(roleMenu.getMenuId());
            });
            roleRes.setMenuIds(menuIds);
            resList.add(roleRes);
        });
        res.put("total", rolePage.getTotal());
        res.put("list", resList);
        return new DataResponse(res);
    }

    @ApiOperation("删除角色")
    @GetMapping("/delRole")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    public DataResponse delRole(@RequestParam Integer roleId) {
        TRole role = roleService.getById(roleId);
        if (role.getRoleType() == 1) {
            // 系统角色不可删除
            return new DataResponse(ErrorCode.E3007, null);
        }
        QueryWrapper<TUser> userWrapper = new QueryWrapper<>();
        userWrapper.eq("role_id", roleId);
        userWrapper.eq("is_delete", 0);
        if (userService.count(userWrapper) > 0) {
            // 角色下还关联用户，禁止删除
            return new DataResponse(ErrorCode.E3012, null);
        }
        role.setIsDelete(1);
        roleService.updateById(role);
        QueryWrapper<TRoleMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        roleMenuService.remove(queryWrapper);
        return new DataResponse();
    }

    @ApiOperation("新增/修改角色")
    @PostMapping("/saveRole")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    public DataResponse saveRole(@RequestBody RoleVo roleVo, @RequestHeader("userId") String userId) {
        QueryWrapper<TRole> roleQueryWrapper = new QueryWrapper<>();
        roleQueryWrapper.eq("is_delete", 0);
        roleQueryWrapper.eq("role_name", roleVo.getRoleName());
        if (roleVo.getId() != null) {
            roleQueryWrapper.ne("id", roleVo.getId());
        }
        List<TRole> roleList = roleService.list(roleQueryWrapper);
        if (CollectionUtil.isNotEmpty(roleList)) {
            return new DataResponse(ErrorCode.E3015, null);
        }

        TRole role = new TRole();
        BeanUtils.copyProperties(roleVo, role);
        if (roleVo.getId() == null) {
            // 新增角色
            role.setCreateBy(Integer.parseInt(userId));
        } else {
            // 编辑角色
            QueryWrapper<TRoleMenu> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("role_id", role.getId());
            roleMenuService.remove(queryWrapper);
        }
        roleService.saveOrUpdate(role);

        List<TRoleMenu> roleMenuList = new ArrayList<>();
        for (Integer menuId : roleVo.getMenuIds()) {
            TRoleMenu roleMenu = new TRoleMenu();
            roleMenu.setRoleId(role.getId());
            roleMenu.setMenuId(menuId);
            roleMenuList.add(roleMenu);
        }
        if (roleMenuList.size() > 0) {
            roleMenuService.saveBatch(roleMenuList);
        }

        return new DataResponse();
    }

}
