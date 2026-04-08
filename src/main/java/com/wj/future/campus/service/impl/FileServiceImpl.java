package com.wj.future.campus.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.future.campus.entity.pojo.FilePojo;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FilePojo> implements FileService {

    @Autowired
    private MinioUtil minioUtil;

    @Value("${minio.bucketName}")
    private String bucketName;


    /*
     上传文件
     */
    @Override
    public String upload(MultipartFile file, HttpServletRequest request) {
        String objectName = minioUtil.upload(file);
        String fileUrl = minioUtil.getFileUrl(objectName);

        FilePojo filePojo = new FilePojo();
        filePojo.setId(IdUtil.getSnowflakeNextId());
        filePojo.setDownloadUrl(fileUrl);
        filePojo.setCreateTime(LocalDateTime.now());
        filePojo.setFileName(file.getName());
        filePojo.setSize(file.getSize());
        filePojo.setBucketName(minioUtil.getBucketName());
        filePojo.setObjectName(objectName);
        save(filePojo);

        return fileUrl;
    }

    /*
    下载文件
     */
    @Override
    public void download(String fileId, HttpServletResponse response) {

    }
}
