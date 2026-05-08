package com.wj.future.campus.init;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.typesense.api.Client;
import org.typesense.model.CollectionSchema;
import org.typesense.model.Field;

import java.util.ArrayList;
import java.util.List;

@Component
public class InitTypesense implements CommandLineRunner {

    public final static Logger logger = LoggerFactory.getLogger(InitTypesense.class);

    @Autowired
    private Client typesenseClient;
    @Override
    public void run(String... args) throws Exception {
        try{
            List<Field> fields = new ArrayList<>();

            // id 字段 (Typesense 默认会有个 string 类型的 id)
            fields.add(new Field().name("id").type("string"));

            // 文本搜索字段
            fields.add(new Field().name("title").type("string").facet(false));
            fields.add(new Field().name("content").type("string").facet(false));

            // 过滤与排序字段
            fields.add(new Field().name("viewRange").type("int32").facet(true));
            fields.add(new Field().name("schoolId").type("string").facet(true));
            fields.add(new Field().name("schoolName").type("string").facet(true));

            // 时间字段 (建议转为 Unix 时间戳存储)
            fields.add(new Field().name("createTime").type("int64").sort(true));
            fields.add(new Field().name("updateTime").type("int64").sort(true).optional(true));

            // 数组字段
            fields.add(new Field().name("photoUrl").type("string[]").index(false).optional(true));
            fields.add(new Field().name("tag").type("string[]").facet(true).optional(true));

            // 数值权重字段
            fields.add(new Field().name("likeCount").type("int32").sort(true));
            fields.add(new Field().name("heat").type("int32").sort(true));

            // 用户信息
            fields.add(new Field().name("sendUserName").type("string").index(false).optional(true));
            fields.add(new Field().name("sendUserId").type("int64").facet(true));

            CollectionSchema collectionSchema = new CollectionSchema();
            collectionSchema.name("article_index")
                    .fields(fields)
                    // 默认排序字段（如果搜索词为空，按热度倒序）
                    .defaultSortingField("heat");

            // 执行创建
            typesenseClient.collections().create(collectionSchema);
        }catch (Exception e){
            logger.warn("已经创建过了,失败原因{}",e.getMessage());
        }
        // 创建向量检索集合（用于 RAG 历史对话检索）
        try {
            List<Field> fields = new ArrayList<>();
                    
            // ==================== 主键字段 ====================
            // id 字段：文档唯一标识（string 类型）
            fields.add(new Field()
                    .name("id")
                    .type("string")
                    .facet(false)
                    .index(true));
                    
            // ==================== 向量字段 ====================
            // user_vector: 用户问题的向量表示
            // Typesense 向量字段类型必须是 "float[]"
            // num_dim 必须与你的 Embedding 模型输出维度一致
            // 假设使用的是 BGE-small-zh 或 All-MiniLM-L6-v2，维度通常是 384 或 512
            int vectorDimension = 384; // 根据你的 EmbeddingModel 实际维度修改
                    
            fields.add(new Field()
                    .name("userVector")
                    .type("float[]")
                    .numDim(vectorDimension)  // 向量维度，必须指定
                    .index(true)  // 向量字段必须可索引
                    .optional(false));  // 向量字段必填
                    
            // bot_vector: AI 回复的向量表示（可选，用于双向检索）
            fields.add(new Field()
                    .name("botVector")
                    .type("float[]")
                    .numDim(vectorDimension)
                    .index(true)
                    .optional(true));  // 可选字段
                    
            // ==================== 文本内容字段 ====================
            // user_msg: 用户原始问题（用于展示和全文搜索）
            fields.add(new Field()
                    .name("userMsg")
                    .type("string")
                    .facet(false)
                    .index(true)  // 支持全文搜索
                    .optional(false));  // 必填

            fields.add(new Field()
                    .name("userMsgSeg")
                    .type("string")
                    .facet(false)
                    .index(true)  // 支持全文搜索
                    .optional(false));
                    
            // bot_msg: AI 回复内容（用于展示和全文搜索）
            fields.add(new Field()
                    .name("botMsg")
                    .type("string")
                    .facet(false)
                    .index(true)
                    .optional(false));  // 可选


            fields.add(new Field()
                    .name("botMsgSeg")
                    .type("string")
                    .facet(false)
                    .index(true)  // 支持全文搜索
                    .optional(false));
                    
            // ==================== 过滤与元数据字段 ====================
            // school_id: 学校ID，用于按学校过滤（个性化检索）
            fields.add(new Field()
                    .name("schoolId")
                    .type("int64")
                    .facet(true)  // 支持分面过滤
                    .index(true)
                    .optional(true));  // 匿名用户可能没有学校ID
                    
            // user_id: 用户ID，用于追踪来源
            fields.add(new Field()
                    .name("userId")
                    .type("int64")
                    .facet(true)
                    .index(true)
                    .optional(true));
                    
            // conversation_id: 会话ID，用于关联同一对话的多轮记录
            fields.add(new Field()
                    .name("conversationId")
                    .type("string")
                    .facet(true)
                    .index(true)
                    .optional(false));  // 必填
                    
            // create_time: 创建时间（Unix 时间戳，用于排序）
            fields.add(new Field()
                    .name("createTime")
                    .type("int64")
                    .sort(true)  // 支持按时间排序
                    .facet(false)
                    .optional(false));  // 必填
                    
            // ==================== 构建集合 Schema ====================
            CollectionSchema collectionSchema = new CollectionSchema();
            collectionSchema
                    .name("source_vector")  // 集合名称
                    .fields(fields)
                    .defaultSortingField("createTime");  // 默认按时间倒序
                    
            // 执行创建
            typesenseClient.collections().create(collectionSchema);
            logger.info("✅ Typesense 向量集合 'source_vector' 创建成功，向量维度: {}", vectorDimension);
                    
        } catch (Exception e) {
            logger.warn("⚠️  Typesense 向量集合 'source_vector' 已存在或创建失败: {}", e.getMessage());
        }
    }
}
