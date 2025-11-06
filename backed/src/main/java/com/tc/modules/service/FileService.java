package com.tc.modules.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Author jiangzhou
 * Date 2023/11/3
 * Description TODO
 **/
public class FileService {

    public static String storePic(MultipartFile file, String picFolder) {
        /*获取文件原名称*/
        String originalFilename = file.getOriginalFilename();
        /*获取文件格式*/
        String fileFormat = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");

        //没有路径就创建路径
        File tmp = new File(picFolder);
        if (!tmp.exists()) {
            tmp.mkdirs();
        }
        String resourcesPath = picFolder + File.separator + uuid + fileFormat;

        File upFile = new File(resourcesPath);
        try {
            file.transferTo(upFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return uuid + fileFormat;
    }
}
