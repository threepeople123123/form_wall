package com.wj.future.campus.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.future.campus.entity.pojo.FilePojo;
import com.wj.future.campus.entity.pojo.UserPojo;
import com.wj.future.campus.entity.response.UploadFileResponse;
import com.wj.future.campus.mapper.FileMapper;
import com.wj.future.campus.service.FileService;
import com.wj.future.campus.util.MinioUtil;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FilePojo> implements FileService {

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /*
     上传文件
     */
    @Override
    public UploadFileResponse upload(MultipartFile file, UserPojo userPojo) {

        String objectName = minioUtil.getObjectName(file.getOriginalFilename());

        long fileId = IdUtil.getSnowflakeNextId();

        String downloadUrl = minioUtil.getDownloadUrl(fileId);

        String fileName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));

        FilePojo filePojo = new FilePojo();
        filePojo.setId(fileId);
        filePojo.setUserId(userPojo.getUserId());
        filePojo.setUserName(userPojo.getUserName());
        filePojo.setDownloadUrl(downloadUrl);
        filePojo.setCreateTime(LocalDateTime.now());
        filePojo.setFileName(fileName);
        filePojo.setSize(file.getSize());
        filePojo.setBucketName(minioUtil.getBucketName());
        filePojo.setObjectName(objectName);
        filePojo.setContentType(file.getContentType());

        // 执行是否成功
        transactionTemplate.execute(status -> {
            boolean save = save(filePojo);

            minioUtil.upload(file,objectName);

            return save;
        });

        UploadFileResponse uploadFileResponse = new UploadFileResponse();
        uploadFileResponse.setDownloadUrl(downloadUrl);
        uploadFileResponse.setId(filePojo.getId());

        return uploadFileResponse;
    }

    /*
    下载文件
     */
    @Override
    public void download(long fileId, HttpServletResponse response) {
        // 下载文件信息
        FilePojo filePojo = getById(fileId);
        minioUtil.download(filePojo,response);
    }
}
