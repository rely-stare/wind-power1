package com.tc.modules.vo;

import com.tc.modules.entity.THardware;
import lombok.Data;

import java.util.List;

@Data
public class MonitorFunVO {

    private String hardwareType;

    private List<THardware> featureList ;
}
