package com.wj.future.campus.controller;

import com.wj.future.campus.result.R;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @RequestMapping("/upload")
    public R<String> upload(@RequestPart("files") List<MultipartFile> files, HttpServletRequest request){


        return R.okMsg("url");
    }
}
