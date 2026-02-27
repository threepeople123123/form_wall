package com.wj.form.wall.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/send")
public class ForwardController {

    @PostMapping("/content")
    public String  content(){
        /**
         * todo：
         *  1,发布内容，前端选择可见范围，编写发布内容，
         */
        return "success";
    }
}
