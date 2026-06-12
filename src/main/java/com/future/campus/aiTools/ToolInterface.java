package com.future.campus.aiTools;

import com.alibaba.dashscope.tools.FunctionDefinition;
import com.google.gson.JsonObject;

public interface ToolInterface {

    JsonObject parameters();

    // 接口查询返回参数
    String execute(String arguments);

    Boolean onlyReader();
}
