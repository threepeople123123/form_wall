package com.wj.future.campus.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.wj.future.campus.entity.pojo.UserPojo;
import com.wj.future.campus.entity.response.UploadFileResponse;
import com.wj.future.campus.exception.FormWallException;
import com.wj.future.campus.result.R;
import com.wj.future.campus.service.FileService;
import com.wj.future.campus.util.TokenBucketLimiter;
import com.wj.future.campus.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * packageName com.wj.form.wall.controller
 *
 * @author wj
 * @className ObjectController
 * @date 2026/2/28
 * @description 对象上传接口
 */
@RestController
@RequestMapping("/object")
public class ObjectController {


    @Autowired
    private FileService fileService;

    @Autowired
    private TokenBucketLimiter tokenBucketLimiter;

    @Autowired
    private UserUtil userUtil;

    @PutMapping("/upload")
    public R<UploadFileResponse> upload(@RequestPart("file") MultipartFile file, HttpServletRequest request) throws FormWallException {
        // 上传文件集合
        if (ObjectUtil.isNotEmpty(file)){
            if (file.getSize() > 5 * 1024 * 1024) {
                throw  new FormWallException("文件大于5m");
            }
            UserPojo user = userUtil.getUser(request);
            UploadFileResponse response = fileService.upload(file, user);
            return R.ok(response);
        }

        return R.failure("文件不能为空",500);
    }

    @GetMapping("/download/{fileId}")
    public void download(@PathVariable("fileId") String fileId, HttpServletResponse response) throws FormWallException {
        if (StrUtil.isBlank(fileId)){
            throw new FormWallException("请选择要下载的文件");
        }

        // 使用令牌桶进行限流
       // boolean pass = tokenBucketLimiter.tryAcquire("download", 5.0, 10);

        // 拿到令牌
//        if (pass){
            fileService.download(Long.valueOf(fileId),response);
//        }
    }
}
