package com.wj.future.campus.rag;

import cn.hutool.core.collection.CollUtil;
import com.wj.future.campus.entity.pojo.rdb.UserPojo;
import com.wj.future.campus.util.EmbeddingUtil;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.typesense.api.Client;
import org.typesense.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.wj.future.campus.controller.AiController.threadLocalUserPojo;

/**
 * 基于 PostgreSQL pgvector 的内容检索器
 * 用于从历史对话中检索相似内容
 */
@Slf4j
@Component
public class TypesenseVectorContentRetriever implements ContentRetriever {

    @Autowired
    private Client typesenseClient;

    @Autowired
    private EmbeddingUtil embedToVector;



    @Override
    public List<Content> retrieve(Query query) {
        UserPojo userPojo = threadLocalUserPojo.get();
        String collectionName = "source_vector";

        // 1. 将查询文本转换为向量
        List<Double> vector = embedToVector.embedToVector(query.text());
        log.info("查询文本: {}, 向量维度: {}", query.text(), vector.size());

        // 2. 构建向量查询字符串
        // 语法修正：field_name:([v1, v2...], k:10)
        // 注意：外层是圆括号，内层是方括号，k 参数建议写在括号内
        String vectorStr = vector.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // 核心修正点：确保格式为 "字段名:([向量], k:数量)"
        String vectorQuery = String.format("userVector:([%s], k:%d)", vectorStr, 10);

        log.debug("向量查询字符串预览: {}", vectorQuery.substring(0, Math.min(60, vectorQuery.length())) + "...");

        // 3. 设置搜索参数
        MultiSearchCollectionParameters parameters = new MultiSearchCollectionParameters();
        parameters.setQ("*"); // 必须设置 q，使用 * 表示匹配所有记录，由向量搜索负责重排
        parameters.setVectorQuery(vectorQuery);
        parameters.setCollection(collectionName);
        parameters.setPerPage(10); // 返回条数

        MultiSearchSearchesParameter searchParameters = new MultiSearchSearchesParameter();
        searchParameters.setSearches(List.of(parameters));

        try {
            // 4. 执行搜索
            MultiSearchResult multiSearchResult = typesenseClient.multiSearch.perform(searchParameters, Map.of());

            // 5. 解析结果并转换为 Content
            if (multiSearchResult.getResults() == null || multiSearchResult.getResults().isEmpty()) {
                return List.of();
            }

            SearchResult result = multiSearchResult.getResults().get(0);
            if (result.getHits() == null) {
                return List.of();
            }

            log.info("TypeSense 搜索完成，命中共 {} 条记录", result.getHits().size());

            List<String> botMsgList  = result.getHits().stream()
                    .map(hit -> {
                        Map<String, Object> document = hit.getDocument();
                        // 这里假设你的文本字段名为 "content"，请根据实际 Schema 修改
                        String text = (String) document.getOrDefault("botMsg", "");

                        // 将元数据也封装进去（如果有需要）
                        Map<String, String> metadata = new HashMap<>();
                        document.forEach((k, v) -> metadata.put(k, String.valueOf(v)));

                        return text;
                    })
                    .collect(Collectors.toList());

            List<Content> contentList = botMsgList.stream()
                    .map(Content::from)
                    .collect(Collectors.toList());
            // todo:发送给rerank模型进行重排
            if (CollUtil.isNotEmpty(botMsgList)){

                for (String s : botMsgList) {
                    Content.from(s);
                }
            }

            return contentList;

        } catch (Exception e) {
            log.error("Typesense 搜索发生异常: {}", e.getMessage(), e);
            // 如果依然报 400，建议打印 e.getResponseBody() 查看具体的 Schema 冲突
            return List.of();
        }
    }
}
