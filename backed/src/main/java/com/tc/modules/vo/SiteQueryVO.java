package com.tc.modules.vo;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Author jiangzhou
 * Date 2023/11/10
 * Description TODO
 **/
@Data
public class SiteQueryVO {

    private String siteName;

    private Integer[] orgIds;

    @NotNull(message = "页码为空")
    private Integer page;

    @NotNull(message = "条数为空")
    @Min(value = 1, message = "最少查询1条")
    @Max(value = 100, message = "最多查询100条")
    private Integer size;
}
