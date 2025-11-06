package com.tc.modules.service;

import com.tc.common.http.DataResponse;
import com.tc.modules.query.CommonQuery;

public interface TAudioService {

    /**
     * 获取音频列表
     *
     * @param query 查询条件，包含可能的筛选选项
     * @param pageNo 页码，表示请求的页面编号
     * @param pageSize 页面大小，表示每页包含的音频数量
     * @return 返回一个包含音频列表的DataResponse对象
     */
    DataResponse getAudioList(CommonQuery query, Integer pageNo, Integer pageSize);

    /**
     * 下载文件
     *
     * @param fileID 文件ID，用于标识要下载的文件
     * @return 返回一个包含文件下载信息的DataResponse对象
     */
    DataResponse downloadFile(String fileID);

    /**
     * 获取音频流
     *
     * @param query 查询条件，包含可能的筛选选项
     * @return 返回一个包含音频流的DataResponse对象
     */
    DataResponse getRealStream(CommonQuery query);



    /**
     * 获取音频流
     *
     * @param query 查询条件，包含可能的筛选选项
     * @return 返回一个包含音频流的DataResponse对象
     */
    DataResponse getRealStreamByType(Integer orgId, Integer monitorType);


}
