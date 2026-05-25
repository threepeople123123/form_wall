package com.future.campus.entity.pojo.rdb;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("ai_to_user_conversation_history")
public class AiToUserConversationHistoryPojo {
    @Schema(description = "主键")
    @TableId("id")
    private Long id;

    @Schema(description = "会话id")
    @TableField("conversation_id")
    private String conversationId;

    @Schema(description = "用户id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private Timestamp createTime;
}
