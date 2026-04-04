package com.wj.future.compus.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wj.future.compus.handler.VectorTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@TableName(value = "conversation_vector", autoResultMap = true)
public class ConversationVectorPojo {
    @Schema(description = "id")
    @TableId(value= "id",type = IdType.ASSIGN_ID)
    private long id;

    @TableField(value = "user_vector",typeHandler = VectorTypeHandler.class)
    @Schema(description = "用户向量")
    private float[] userVector;

    @TableField(value = "user_msg")
    @Schema(description = "用户输入")
    private String userMsg;

    @Schema(description = "ai 向量化")
    @TableField(value = "bot_vector",typeHandler = VectorTypeHandler.class)
    private float[] botVector;

    @TableField(value = "bot_msg")
    @Schema(description = "ai输出")
    private String botMsg;

    @Schema(description = "学校 id")
    @TableField(value = "school_id")
    private  String schoolId;

}
