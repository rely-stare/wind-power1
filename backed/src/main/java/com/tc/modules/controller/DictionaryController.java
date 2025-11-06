package com.tc.modules.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tc.common.http.DataResponse;
import com.tc.modules.entity.TDictionary;
import com.tc.modules.service.TDictionaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author jiangzhou
 * Date 2023/11/8
 * Description TODO
 **/
@RestController
@Api(tags = "数据字典")
@RequestMapping("/dictionary")
public class DictionaryController {

    @Resource
    private TDictionaryService dictionaryService;

    @ApiOperation("查询数据字典")
    @GetMapping("/getDictionary")
    public DataResponse getDictionary(@RequestParam(required = false) String type) {
        QueryWrapper<TDictionary> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(type)) {
            queryWrapper.eq("type", type);
        }
        List<TDictionary> list = dictionaryService.list(queryWrapper);
        Map<String, List> res = new HashMap<>();
        list.stream().forEach(dictionary -> {
            List typeDic = res.get(dictionary.getType());
            if (typeDic == null) {
                typeDic = new ArrayList();
            }
            Map<String, String> temp = new HashMap<>();
            temp.put("key", dictionary.getParamKey());
            temp.put("value", dictionary.getParamValue());
            typeDic.add(temp);
            res.put(dictionary.getType(), typeDic);
        });
        return new DataResponse(res);
    }
}
