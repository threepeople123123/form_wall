package com.future.campus.entity.pojo.rdb;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

import static com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID;

@Data
@TableName("ai_to_user_conversation")
public class AiToUserConversationPoJo {

    @Schema
    @TableId(type = ASSIGN_ID)
    private Long id;

    @Schema(description = "机器人")
    @TableField("bot")
    private String bot;

    @Schema(description = "用户")
    @TableField("\"user\"")
    private String user;

    @Schema(description = "对话id")
    @TableField("conversation_id")
    private String conversationId;

    @Schema(description = "用户id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

}
