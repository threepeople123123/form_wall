package com.wj.future.campus.aiTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AllTools {

    public static final Logger logger = LoggerFactory.getLogger(AllTools.class);


    @Autowired
    private List<ToolInterface> allTools;

    /**
     * 获取所有工具实现类
     */
    public List<ToolInterface> getAllTools() {
        return allTools;
    }

    /**
     * 根据工具名称获取工具
     */
    public ToolInterface getTool(String name) {
        if (name == null || allTools == null) {
            return null;
        }
        for (ToolInterface tool : allTools) {
            if (tool.getName().equals(name)) {
                return tool;
            }
        }
        return null;
    }
}
