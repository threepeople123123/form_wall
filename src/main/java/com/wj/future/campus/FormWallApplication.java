package com.wj.future.campus;

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
