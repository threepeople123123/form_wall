package com.wj.future.campus.util;

import cn.hutool.core.util.IdUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class MinioUtil {

    @Value("${minio.bucketName}")
    private String BUCKET_NAME;

    @Autowired
    private MinioClient minioClient;

    public String getObjectName(String originalFilename){
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "files/" + timestamp + "_" + IdUtil.getSnowflakeNextId() + extension;
    }

    public String upload(MultipartFile file){
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        try (InputStream inputStream = file.getInputStream()) {
            String objectName = getObjectName(file.getOriginalFilename());

            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();

            minioClient.putObject(putObjectArgs);

            return objectName;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        } catch (ErrorResponseException e) {
            throw new RuntimeException("MinIO响应错误: " + e.getMessage(), e);
        } catch (InsufficientDataException e) {
            throw new RuntimeException("数据不足: " + e.getMessage(), e);
        } catch (InternalException e) {
            throw new RuntimeException("MinIO内部错误: " + e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("无效的密钥: " + e.getMessage(), e);
        } catch (InvalidResponseException e) {
            throw new RuntimeException("无效的响应: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("算法错误: " + e.getMessage(), e);
        } catch (ServerException e) {
            throw new RuntimeException("服务器错误: " + e.getMessage(), e);
        } catch (XmlParserException e) {
            throw new RuntimeException("XML解析错误: " + e.getMessage(), e);
        }
    }

    public String getFileUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    io.minio.GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .expiry(24 * 60 * 60)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("获取文件URL失败: " + e.getMessage(), e);
        }
    }

    public String getBucketName(){
        return BUCKET_NAME;
    }
}
