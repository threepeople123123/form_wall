package com.wj.future.campus.entity.es.po;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;


@Document(indexName =  "ai_chat_knowledge")
@Data
public class KnowledgeDoc {

    @Id
    private String id;

    @Field(name = "content",type = FieldType.Text)
    private String content;

    @Field(name = "embedding",type = FieldType.Dense_Vector)
    private List<Double> embedding;

    private String sessionId;

    private LocalDateTime createTime;

}