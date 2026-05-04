package com.wj.future.campus.util;

import cn.hutool.core.util.IdUtil;
import com.wj.future.campus.entity.pojo.rdb.FilePojo;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.errors.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
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

    @Value("${minio.downloadUrl}")
    private String downloadUrl;

    public String getObjectName(String originalFilename){
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "files/" + timestamp + "_" + IdUtil.getSnowflakeNextId() + extension;
    }

    public void upload(MultipartFile file, String objectName){
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        try (InputStream inputStream = file.getInputStream()) {

            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();

            minioClient.putObject(putObjectArgs);


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

    public void download(FilePojo filePojo, HttpServletResponse response){
        InputStream inputStream = null;
        String objectName = filePojo.getObjectName();
        ServletOutputStream outputStream = null;
        try {
            // 从MinIO获取文件流
            inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(objectName)
                    .build()
            );
                
            // 设置响应头
            String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);
            response.setContentType(filePojo.getContentType());
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                java.net.URLEncoder.encode(fileName, "UTF-8"));
                
            // 将文件流写入响应输出流
            outputStream = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        } finally {
            // 关闭流
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("关闭流失败: " + e.getMessage(), e);
            }
        }
    }


    public String getBucketName(){
        return BUCKET_NAME;
    }

    public  String getDownloadUrl(long fileId){
        return downloadUrl + fileId;
    }
}
