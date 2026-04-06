package com.wj.future.campus.entity.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginResponse {
    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "手机号")
    private String email;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "用户头像")
    private String userHeadUrl;

    @Schema(description = "学校名称")
    private String schoolName;

    @Schema(description = "学校id")
    private String schoolId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "token")
    private String token;
}
