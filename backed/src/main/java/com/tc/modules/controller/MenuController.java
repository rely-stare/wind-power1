package com.tc.modules.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.tc.modules.entity.TMenu;
import com.tc.modules.entity.TRoleMenu;
import com.tc.modules.entity.TUser;
import com.tc.modules.service.TMenuService;
import com.tc.modules.service.TRoleMenuService;
import com.tc.modules.service.TUserService;
import com.tc.common.http.DataResponse;
import com.tc.modules.vo.*;
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
 * Author jiangzhou
 * Date 2023/10/10
 * Description 菜单
 **/
@RestController
@Api(tags = "菜单管理")
@RequestMapping("/menu")
public class MenuController {

    @Resource
    private TUserService userService;
    @Resource
    private TMenuService menuService;
    @Resource
    private TRoleMenuService roleMenuService;

    @ApiOperation("根据用户ID查询菜单")
    @GetMapping("/getMenu")
    public DataResponse getMenu(@RequestHeader("userId") String userId) {
        TUser user = userService.getById(userId);
        QueryWrapper<TRoleMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", user.getRoleId());
        List<TRoleMenu> roleMenuList = roleMenuService.list(queryWrapper);
        Integer[] menuIds = roleMenuList.stream().map(TRoleMenu::getMenuId).toArray(Integer[]::new);
        QueryWrapper<TMenu> menuWrapper = new QueryWrapper<>();
        menuWrapper.in("id", menuIds).eq("status", 1);
        List<TMenu> list = menuService.list(menuWrapper);
        List<HomeMenuVO> res = new ArrayList<>();
        list.stream().forEach(menu -> {
            HomeMenuVO homeMenuVO = new HomeMenuVO();
            Meta meta = new Meta();
            BeanUtils.copyProperties(menu, meta);
            BeanUtils.copyProperties(menu, homeMenuVO);
            homeMenuVO.setMeta(meta);
            res.add(homeMenuVO);
        });
        return new DataResponse(res);
    }

    @ApiOperation("查询菜单列表")
    @PostMapping("/getMenuList")
    public DataResponse getMenuList(@RequestBody MenuQueryVO menuQueryVO) {
        QueryWrapper<TMenu> menuWrapper = new QueryWrapper<>();
        if (menuQueryVO.getStatus() != null) {
            menuWrapper.eq("status", menuQueryVO.getStatus());
        }
        if (menuQueryVO.getAccessTypeList() != null && menuQueryVO.getAccessTypeList().length > 0) {
            menuWrapper.in("access_type", menuQueryVO.getAccessTypeList());
        }
        if (!StringUtils.isBlank(menuQueryVO.getTitle())) {
            menuWrapper.like("title", menuQueryVO.getTitle());
            List<TMenu> list = menuService.list(menuWrapper);
            Map<Integer, TMenu> resList = new HashMap<>();
            list.stream().forEach(menu -> {
                resList.put(menu.getId(), menu);
                while (menu.getParentId() != null) {
                    TMenu parentMenu = menuService.getById(menu.getParentId());
                    resList.put(parentMenu.getId(), parentMenu);
                    menu = parentMenu;
                }
            });
            list.stream().forEach(menu -> {
                QueryWrapper<TMenu> childWrapper = new QueryWrapper<>();
                childWrapper.eq("parent_id", menu.getId());
                List<TMenu> childMenuList = menuService.list(childWrapper);
                childMenuList.stream().forEach(childMenu -> {
                    resList.put(childMenu.getId(), childMenu);
                    QueryWrapper<TMenu> grandChildWrapper = new QueryWrapper<>();
                    grandChildWrapper.eq("parent_id", childMenu.getId());
                    List<TMenu> grandChildMenuList = menuService.list(grandChildWrapper);
                    grandChildMenuList.stream().forEach(grandChildMenu -> {
                        resList.put(grandChildMenu.getId(), grandChildMenu);
                    });
                });
            });
            return new DataResponse(resList.values());
        }
        return new DataResponse(menuService.list(menuWrapper));
    }

    @ApiOperation("启用菜单")
    @PostMapping("/enableMenu")
    public DataResponse enableMenu(@RequestBody EnableMenuVo enableMenuVo) {
        UpdateWrapper<TMenu> menuWrapper = new UpdateWrapper<>();
        menuWrapper.eq("id", enableMenuVo.getMenuId()).set("status", enableMenuVo.getStatus());
        menuService.update(menuWrapper);
        handlerChild(enableMenuVo.getMenuId(), enableMenuVo.getStatus());
        return new DataResponse();
    }

    public void handlerChild(Integer parentId, Integer status) {
        QueryWrapper<TMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId);
        List<TMenu> childList = menuService.list(queryWrapper);
        childList.stream().forEach(menu -> {
            menu.setStatus(status);
            menuService.updateById(menu);
            handlerChild(menu.getId(), status);
        });
    }

    @ApiOperation("修改菜单")
    @PostMapping("/updateMenu")
    public DataResponse updateMenu(@RequestBody UpdateMenuVO updateMenuVO) {
        TMenu tMenu = new TMenu();
        BeanUtils.copyProperties(updateMenuVO, tMenu);
        return new DataResponse(menuService.updateById(tMenu));
    }

}
