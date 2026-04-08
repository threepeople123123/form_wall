package com.wj.future.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wj.future.campus.entity.pojo.FilePojo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService extends IService<FilePojo> {

    /*
    上传文件
     */
    String upload(MultipartFile file, HttpServletRequest request);

    /*
    下载文件
     */
    void download(String fileId, HttpServletResponse response);
}
