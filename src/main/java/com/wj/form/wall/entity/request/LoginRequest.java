package com.wj.form.wall.entity.request;


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

    @Schema(name = "用户名",description = "用户名")
    private String userName;

    @Schema(name = "手机号",description = "手机号")
    private String phone;

    @Schema(name = "密码",description = "密码")
    private String password;

    @Schema(name = "设备类型",description = "设备类型")
    private String deviceType;

}
