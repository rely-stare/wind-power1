package com.tc.modules.common.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TSite {

    private Integer id;

    private String siteName;

    private String siteModel;

    private Integer orgId;

    private Integer isDelete;

    private String createBy;

    private Date createTime;
}
