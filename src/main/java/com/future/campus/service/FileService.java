package com.future.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.future.campus.entity.pojo.rdb.FilePojo;
import com.future.campus.entity.pojo.rdb.UserPojo;
import com.future.campus.entity.response.UploadFileResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService extends IService<FilePojo> {

    /*
    上传文件
     */
    UploadFileResponse upload(MultipartFile file, UserPojo userPojo);

    /*
    下载文件
     */
    void download(long fileId, HttpServletResponse response);
}
