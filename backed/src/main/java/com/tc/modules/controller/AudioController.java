package com.tc.modules.controller;

import com.alibaba.fastjson.JSONObject;
import com.tc.common.http.DataResponse;
import com.tc.common.utils.HttpUtils;
import com.tc.modules.query.CommonQuery;
import com.tc.modules.service.TAudioService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedReader;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "音频管理")
@RestController
@RequestMapping("/audio")
public class AudioController {

    @Value("${audio.url}")
    private String audioUrl;

    @Value("${audio.api-key}")
    private String audioApiKey;

    @Value("${audio.api-secret}")
    private String audioApiSecret;

    @Autowired
    private TAudioService audioService;


    @GetMapping("/list")
    @ApiOperation(value = "获取音频列表")
    public DataResponse getAudioList(CommonQuery query,
                                     @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return audioService.getAudioList(query, pageNum, pageSize);
    }

    @GetMapping("/live")
    @ApiOperation(value = "获取实时音频流")
    public DataResponse<JSONObject> realStream(CommonQuery query) {
        return audioService.getRealStream(query);
    }

    @GetMapping("/liveByType")
    @ApiOperation(value = "获取实时音频流")
    public DataResponse<JSONObject> realStreamByType(Integer orgId, Integer monitorType) {
        return audioService.getRealStreamByType(orgId, monitorType);
    }


    @GetMapping("/downloadFile")
    @ApiOperation(value = "下载音频文件")
    public DataResponse downloadFile(String fileID) {
        return audioService.downloadFile(fileID);
    }

    /**
     * 音频文件流式传输（小文件）
     *
     * @param fileID
     * @param response
     * @throws IOException
     */

    @GetMapping("/stream/{fileID}")
    @ApiOperation(value = "音频文件流式传输（小文件）")
    public void streamAudio(@PathVariable String fileID, HttpServletResponse response) throws IOException {
        String url = audioUrl + "/api/v1/file/download/single" + "?fileID=" + fileID;

        Map<String, String> headers = new HashMap<>();
        headers.put(audioApiKey, audioApiSecret);

        ResponseEntity<byte[]> responseEntity = HttpUtils.sendGet(url, headers, byte[].class);
        byte[] audioBytes = responseEntity.getBody();

        try (OutputStream outputStream = response.getOutputStream()) {
            if (audioBytes != null) {
                outputStream.write(audioBytes);
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 音频文件流式传输（大文件）
     *
     * @param fileID
     * @param response
     * @return
     * @throws IOException
     */
    @ApiOperation(value = "音频文件流式传输（大文件）")
    @GetMapping("/stream2/{fileID}")
    public ResponseEntity<StreamingResponseBody> streamAudio2(@PathVariable String fileID, HttpServletResponse response) throws IOException {

        String url = audioUrl + "/api/v1/file/download/single" + "?fileID=" + fileID;
        Map<String, String> headers = new HashMap<>();
        headers.put(audioApiKey, audioApiSecret);

        ResponseEntity<Resource> audioResponse = HttpUtils.sendGet(url, headers, Resource.class);

        InputStream audioInputStream = audioResponse.getBody().getInputStream();

        // 使用 StreamingResponseBody 进行流式传输
        StreamingResponseBody streamingResponseBody = outputStream -> {
            byte[] buffer = new byte[8192];  // 设置缓冲区大小
            int bytesRead;
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead); // 将音频数据写入响应流
            }
            audioInputStream.close();  // 完成后关闭流
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav")) // 设置音频格式
                .body(streamingResponseBody)
                ;
    }
}
