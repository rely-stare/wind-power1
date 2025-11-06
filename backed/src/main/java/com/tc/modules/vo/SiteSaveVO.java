package com.tc.modules.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Author jiangzhou
 * Date 2023/11/10
 * Description TODO
 **/
@Data
public class SiteSaveVO {

    @NotEmpty(message = "请先在组织架构中添加站点")
    private Integer id;

    @NotEmpty(message = "风机名称不可为空")
    private String siteName;

    @NotEmpty(message = "风机型号不可为空")
    private String siteModel;

//    @NotNull(message = "风机类型不可为空")
//    private Integer siteType;

    private Integer orgId;

}
