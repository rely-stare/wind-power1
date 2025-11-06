package com.tc.modules.vo;

import lombok.Data;

/**
 * Author jiangzhou
 * Date 2023/11/8
 * Description TODO
 **/
@Data
public class UpdateMenuVO {

    private Integer id;

    private Integer parentId;

    private Integer menuType;

    private String title;

    private String iconPic;

    private String name;

    private String path;

    private Integer sort;

    private Integer isShow;

    private String redirect;

    private Integer status;

    private Integer isFrame;

    private Integer isCache;

    private Integer accessType;

    private String component;
}
