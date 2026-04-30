package com.wj.future.campus.aiTools;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
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

    /**
     * 将所有工具转换为 LangChain4j 的 ToolSpecification 列表
     * 用于注册到 AiServices 中，使 LLM 能够识别和调用这些工具
     *
     * @return ToolSpecification 列表
     */
    public List<ToolSpecification> getAllToolSpecifications() {
        if (allTools == null || allTools.isEmpty()) {
            logger.warn("没有可用的工具");
            return new ArrayList<>();
        }

        List<ToolSpecification> specifications = new ArrayList<>();
        for (ToolInterface tool : allTools) {
            try {
                // 从工具实例中自动提取所有带有 @Tool 注解的方法
                List<ToolSpecification> toolSpecs = ToolSpecifications.toolSpecificationsFrom(tool);
                specifications.addAll(toolSpecs);
                logger.info("成功加载工具: {}, 方法数: {}", tool.getName(), toolSpecs.size());
            } catch (Exception e) {
                logger.error("加载工具失败: {}", tool.getName(), e);
            }
        }

        logger.info("总共加载 {} 个工具规范", specifications.size());
        return specifications;
    }

    /**
     * 根据工具名称获取对应的 ToolSpecification
     *
     * @param name 工具名称
     * @return ToolSpecification，如果未找到则返回 null
     */
    public ToolSpecification getToolSpecificationByName(String name) {
        if (name == null || allTools == null) {
            return null;
        }

        for (ToolInterface tool : allTools) {
            if (tool.getName().equals(name)) {
                try {
                    List<ToolSpecification> specs = ToolSpecifications.toolSpecificationsFrom(tool);
                    // 查找名称匹配的规格
                    return specs.stream()
                            .filter(spec -> spec.name().equals(name))
                            .findFirst()
                            .orElse(null);
                } catch (Exception e) {
                    logger.error("获取工具规范失败: {}", name, e);
                    return null;
                }
            }
        }
        return null;
    }
}
