package com.future.campus.entity.pojo.nosql;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.print.DocFlavor;
import java.util.List;

/**
 * Typesense 向量检索集合实体类
 * 对应集合名称: source_vector
 * 用于 RAG 历史对话检索
 */
@Data
@Schema(description = "Typesense向量索引实体")
public class SourceVectorIndex {

    @Schema(description = "文档唯一标识")
    private Long id;

    @Schema(description = "用户问题向量（维度需与Embedding模型一致，通常为384）")
    private List<Float> userVector;

    @Schema(description = "AI回复向量（可选，用于双向检索）")
    private List<Float> botVector;

    @Schema(description = "用户原始问题")
    private String userMsg;

    @Schema(description = "AI回复内容")
    private String botMsg;

    @Schema(description = "用户原始问题分词")
    private String userMsgSeg;

    @Schema(description = "AI回复内容分词")
    private String botMsgSeg;

    @Schema(description = "学校ID（用于按学校过滤）")
    private String schoolId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "会话ID（关联同一对话的多轮记录）")
    private String conversationId;

    @Schema(description = "创建时间（Unix时间戳，毫秒）")
    private Long createTime;

    public void setUserVectorForDouble(List<Double> userVector){
        if (userVector != null){
            // double转成float
            this.userVector = userVector.stream().map(Double::floatValue).toList();
        }
    }

    public void setUserVectorForFloat(List<Float> userVector){
        if (userVector != null){
            // double转成float
            this.userVector = userVector;
        }
    }

    public void setBotVectorForDouble(List<Double> botVector){
        if (botVector != null){
            // double转成float
            this.botVector = botVector.stream().map(Double::floatValue).toList();
        }
    }

    public void setBotVectorForFloat(List<Float> botVector){
        if (botVector != null){
            // double转成float
            this.botVector = botVector;
        }
    }
}
