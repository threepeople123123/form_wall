package com.wj.future.campus.entity.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * packageName com.wj.form_wall.entity.request
 *
 * @author wj
 * @className LoginRequest
 * @date 2026/2/27
 * @description 登录请求接收类
 */
@Data
public class LoginRequest {

    @Schema(name = "userName",description = "用户名")
    private String userName;

    @Schema(name = "phone",description = "手机号")
    private String phone;

    @Schema(name = "password",description = "密码")
    private String password;

    @Schema(name = "deviceType",description = "设备类型")
    private String deviceType;

}
