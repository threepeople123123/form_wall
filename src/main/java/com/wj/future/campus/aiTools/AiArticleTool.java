package com.wj.future.campus.aiTools;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.tools.FunctionDefinition;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wj.future.campus.controller.ArticleController;
import com.wj.future.campus.entity.pojo.UserPojo;
import com.wj.future.campus.entity.request.ArticleRequest;
import com.wj.future.campus.entity.response.ArticleResponse;
import com.wj.future.campus.result.R;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

import static com.wj.future.campus.controller.AiController.threadLocalUserPojo;

@Slf4j
@Component
public class AiArticleTool implements ToolInterface {

    @Autowired
    private ArticleController articleController;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String DESCRIPTION = "搜索校园文章，可以根据用户输入的关键字搜索文章";


    public String getName(){
        return "search_articles";
    }

    public JsonObject parameters(){
        // keyword 参数定义
        JsonObject keyword = new JsonObject();
        keyword.addProperty("type", "string");
        keyword.addProperty("description", "搜索文章关键词");

        // properties
        JsonObject properties = new JsonObject();
        properties.add("query", keyword);

        // parameters
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        parameters.add("properties", properties);

        // required
        JsonArray required = new JsonArray();
        required.add("query");

        return parameters;
    }

    /**
     * 获取工具定义（用于 DashScope SDK）
     */
    public FunctionDefinition definition() {
        return FunctionDefinition.builder()
                .name(getName())
                .description(DESCRIPTION)
                .parameters(parameters())
                .build();
    }

    /**
     * 搜索文章工具（DashScope SDK 调用）
     */
    public String execute(String arguments) {
        try {
            UserPojo userPojo = threadLocalUserPojo.get();
            log.info("调用搜索文章工具，参数: {}", arguments);
            JSONObject jsonObject = JSONUtil.parseObj(arguments);

            String query = jsonObject.getStr("query");
            List<String> tag = jsonObject.getBeanList("tag", String.class);


            // 调用实际的搜索逻辑
            ArticleRequest request = new ArticleRequest();
            request.setQuery(query);
            if (tag != null && !tag.isEmpty()) {
                request.setTag(tag);
            }

            R<Page<ArticleResponse>> result = articleController.pageList(request);

            // 将结果转换为字符串返回给AI
            String resultStr = JSONUtil.toJsonStr(result.getData());
            log.info("搜索结果: {}", resultStr);
            return resultStr;
        } catch (Exception e) {
            log.error("搜索文章失败", e);
            return "{\"error\": \"搜索失败: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 搜索文章工具（LangChain4j 调用）
     */
    @Tool(value = """
            搜索校园帖子，可以根据关键词、标签等条件查找相关文章
            """,name = "search_articles")
    public String searchArticlesByLangChain(@P("查询文章的标题，内容关键字，可以根据该字段查询，非必传") String query
            ,@P("标签集合，可以根据标签查询文章，非必传") List<String> tag) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("query", query);
        jsonObject.put("tag", tag);
        return execute(jsonObject.toString());
    }
}