package com.future.campus.entity.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "用户名称")
    private String userName;

    @Schema(description = "用户头像")
    private String userHeadUrl;

    @Schema(description = "学校名称")
    private String schoolName;

    @Schema(description = "学校id")
    private String schoolId;
}
