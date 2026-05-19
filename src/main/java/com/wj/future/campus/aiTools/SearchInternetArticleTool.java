package com.wj.future.campus.aiTools;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.tools.FunctionDefinition;
import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class SearchInternetArticleTool implements ToolInterface{

    @Tool(
            """
            搜索外部文章，需要区分类型，如所有知网文章，从知乎搜索文章等。
            """
    )
    public String searchInternetArticle(@P("搜索不同的网站的类型，枚举类型为：知网，知乎") String type , @P("搜索文章的关键词") String query) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("query", query);
        jsonObject.put("type", type);
        return execute(jsonObject.toString());
    }

    @Override
    public String getName() {
        return "search_internet_article";
    }

    @Override
    public JsonObject parameters() {
        return null;
    }

    @Override
    public FunctionDefinition definition() {
        return null;
    }

    @Override
    public String execute(String arguments) {
        JSONObject jsonObject = JSONUtil.parseObj(arguments);

        String query = jsonObject.getStr("query");
        String type = jsonObject.getStr("type");
        return "";
    }
}
