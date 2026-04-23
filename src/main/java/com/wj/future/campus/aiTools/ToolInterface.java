package com.wj.future.campus.aiTools;

import com.alibaba.dashscope.tools.FunctionDefinition;
import com.google.gson.JsonObject;

public interface ToolInterface {


    String getName();

    JsonObject parameters();

    // 定义函数
    FunctionDefinition definition();

    // 接口查询返回参数
    String execute(String arguments);
}
