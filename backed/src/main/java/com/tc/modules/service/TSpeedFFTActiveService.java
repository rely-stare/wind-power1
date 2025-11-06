package com.tc.modules.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tc.modules.entity.TSpeedFFTActive;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface TSpeedFFTActiveService extends IService<TSpeedFFTActive> {

    /**
     * 频谱导出
     *
     * @param id
     * @return
     */
    ResponseEntity<byte[]> AnalysisExport(Integer id, Integer alarmId) throws IOException;
}
