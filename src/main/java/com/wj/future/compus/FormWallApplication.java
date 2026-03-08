package com.wj.future.compus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class FormWallApplication {

    public static void main(String[] args) {
        SpringApplication.run(FormWallApplication.class, args);
    }

}
