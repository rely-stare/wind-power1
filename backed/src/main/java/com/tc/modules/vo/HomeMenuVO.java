package com.tc.modules.vo;

import lombok.Data;

/**
 * Author jiangzhou
 * Date 2023/11/23
 * Description TODO
 **/
@Data
public class HomeMenuVO {

    private Integer id;

    private Integer parentId;

    private String name;

    private String path;

    private String redirect;

    private String component;

    private Meta meta;
}
