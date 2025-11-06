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
public class OrgSaveVO {

    private Integer id;

    private Integer parentId;

    @NotEmpty(message = "组织名称不可为空")
    private String orgName;

    // @NotEmpty(message = "组织编码不可为空")
    private String orgCode;

    @NotNull(message = "组织类型不可为空")
    private Integer orgType;
}
