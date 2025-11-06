package com.tc.modules.vo;

import lombok.Data;

/**
 * Author jiangzhou
 * Date 2023/11/8
 * Description TODO
 **/
@Data
public class MenuQueryVO {

    private Integer status;

    private Integer[] accessTypeList;

    private String title;
}
