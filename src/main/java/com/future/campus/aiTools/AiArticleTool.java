package com.future.campus.aiTools;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.tools.FunctionDefinition;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.future.campus.controller.ArticleController;
import com.future.campus.entity.request.ArticleRequest;
import com.future.campus.entity.response.ArticleResponse;
import com.future.campus.result.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;


@Slf4j
@Component
public class AiArticleTool implements ToolInterface , ToolDefinition {

    @Autowired
    private ArticleController articleController;

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
            return  String.format("error:搜索文章失败: %s", e.getMessage());
        }
    }

    @Override
    public Boolean onlyReader() {
        return true;
    }

    /**
     * 搜索文章工具（LangChain4j 调用）
     */
    public String searchArticlesByLangChain(String query,List<String> tag) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("query", query);
        jsonObject.put("tag", tag);
        return execute(jsonObject.toString());
    }

    @Override
    public String name() {
        return "search_articles";
    }

    @Override
    public String description() {
        return "搜索校园帖子，可以根据关键词、标签等条件查找相关文章";
    }

    @Override
    public String inputSchema() {
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

        return parameters.toString();
    }
}