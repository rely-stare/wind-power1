package com.tc.modules.controller;

import com.tc.common.core.controller.BaseController;
import com.tc.common.core.page.TableDataInfo;
import com.tc.modules.entity.TSysOperLog;
import com.tc.modules.service.ISysOperLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 操作日志记录
 *
 * @author tetrabot
 */
@Api(tags = "操作日志")
@RestController
@RequestMapping("/operlog")
public class SysOperlogController extends BaseController {
    @Resource
    private ISysOperLogService iSysOperLogService;

    @ApiOperation("查询操作日志")
    @GetMapping("/list")
    public TableDataInfo list(TSysOperLog po) {
        startPage();
        List<TSysOperLog> TSysOperLogs = iSysOperLogService.selectOperLogList(po);
        return getDataTable(TSysOperLogs);
    }

}
