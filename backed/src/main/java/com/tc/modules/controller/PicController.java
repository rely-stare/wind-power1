package com.tc.modules.controller;

import com.tc.common.http.DataResponse;
import com.tc.common.http.ErrorCode;
import com.tc.modules.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * Author jiangzhou
 * Date 2023/11/3
 * Description 上传图片
 **/
@RestController
@Api(tags = "图片上传")
@RequestMapping("/pic")
public class PicController {

    @Value("${file.pic.folder}")
    private String picFolder;

    @Value("${file.pic.url}")
    private String picUrlPrfix;

    @ApiOperation("上传图片")
    @PostMapping("/uploadPic")
    public DataResponse uploadPic(@RequestParam("file") MultipartFile file) {
        String fileName = FileService.storePic(file, picFolder);
        if (fileName == null) {
            return new DataResponse(ErrorCode.E3005, null);
        } else {
            return new DataResponse(picUrlPrfix + File.separator + fileName);
        }
    }

}
