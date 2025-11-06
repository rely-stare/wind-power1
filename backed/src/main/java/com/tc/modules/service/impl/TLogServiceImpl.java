package com.tc.modules.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tc.modules.entity.TLog;
import com.tc.modules.mapper.TLogMapper;
import com.tc.modules.service.TLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Jz
 * @since 2023-10-17
 */
@Service
public class TLogServiceImpl extends ServiceImpl<TLogMapper, TLog> implements TLogService {

}
