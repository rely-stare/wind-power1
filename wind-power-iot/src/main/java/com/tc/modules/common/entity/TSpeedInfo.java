package com.tc.modules.common.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TSpeedInfo {

    private Integer id;

    private String speedName;

    private String ip;

    private Integer port;

    private Integer slaveId;

    private Integer siteId;

    private Integer isDelete;

    private Integer createBy;

    private Date createTime;

    private Integer updateBy;

    private Date updateTime;
}
