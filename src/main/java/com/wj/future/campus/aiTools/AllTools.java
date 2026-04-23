package com.wj.future.campus.aiTools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AllTools {

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
