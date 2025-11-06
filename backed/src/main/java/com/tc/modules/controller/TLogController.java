package com.tc.modules.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tc.modules.entity.TLog;
import com.tc.common.http.DataResponse;
import com.tc.modules.service.TLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sunchao
 * @date 2024/2/29 17:39
 */
@Api(tags = "日志")
@RestController
@RequestMapping("/log")
public class TLogController {

    @Autowired
    private TLogService tLogService;

    @ApiOperation("查询日志列表")
    @GetMapping("/get.log.list")
    public DataResponse getLogList(TLog po) {
        QueryWrapper<TLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(!ObjectUtils.isEmpty(po.getUserId()), "user_id", po.getUserId());
        queryWrapper.like(!ObjectUtils.isEmpty(po.getIpAddress()), "ip_address", po.getIpAddress());
        queryWrapper.gt(!ObjectUtils.isEmpty(po.getRequestTime()), "request_time", po.getRequestTime());
        queryWrapper.lt(!ObjectUtils.isEmpty(po.getExecuteTime()), "execute_time", po.getExecuteTime());
        queryWrapper.in(!ObjectUtils.isEmpty(po.getResponseCodes()), "response_code", po.getResponseCodes());
        Map<String, Object> res = new HashMap<>();
        Page<TLog> pageInfo = new Page<>(po.getPage(), po.getSize());
        IPage<TLog> page = tLogService.page(pageInfo, queryWrapper);
        res.put("total", page.getTotal());
        res.put("list", page.getRecords());
        return new DataResponse(res);
    }
}
