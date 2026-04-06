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
            fields.add(new Field().name("schoolId").type("string").facet(true)); // 假设这是你过滤用的
            fields.add(new Field().name("schoolName").type("string").facet(true));

            // 时间字段 (建议转为 Unix 时间戳存储)
            fields.add(new Field().name("createTime").type("int64").sort(true));

            // 数组字段
            fields.add(new Field().name("photoUrl").type("string[]").index(false).optional(true));
            fields.add(new Field().name("tag").type("string[]").facet(true));

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
            logger.error("已经创建过了");
        }
    }
}
