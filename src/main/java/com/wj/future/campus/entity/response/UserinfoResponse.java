package com.wj.future.campus.entity.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * packageName com.wj.form.wall.entity.response
 *
 * @author wj
 * @className UserinfoResponse
 * @date 2026/2/28
 * @description 用户信息返回
 */
@Data
public class UserinfoResponse {

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "手机号")
    private String phone;

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
}
