package com.wj.future.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wj.future.campus.entity.pojo.rdb.FilePojo;
import com.wj.future.campus.entity.pojo.rdb.UserPojo;
import com.wj.future.campus.entity.response.UploadFileResponse;
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
