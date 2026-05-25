package com.future.campus.entity.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class ResetPasswordRequest {

    @Schema(name = "email",description = "邮箱")
    private String email;

    @Schema(name = "password",description = "密码")
    private String password;

    @Schema(name = "verificationCode",description = "验证码")
    private String verificationCode;

    @Schema(name = "confirmPassword",description = "确认密码")
    private String confirmPassword;

}
