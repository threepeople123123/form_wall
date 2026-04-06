package com.wj.future.campus.controller;


import com.wj.future.campus.campusEnum.RSAConst;
import com.wj.future.campus.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rsa")
public class RSAController {

    /*
    获取公钥接口
     */
    @GetMapping("/publicKey")

    public R<String> getRSA(){
        return R.ok(RSAConst.PUBLIC_KEY);
    }
}
