package com.wj.future.campus.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID;

@Data
@TableName("ai_to_user_conversation")
public class AiToUserConversationPo {

    @Schema
    @TableId(type = ASSIGN_ID)
    private Long id;

    @Schema(description = "机器人")
    @TableField("bot")
    private String bot;

    @Schema(description = "用户")
    @TableField("user")
    private String user;

    @Schema(description = "对话id")
    @TableField("conversation_id")
    private String conversationId;
}
