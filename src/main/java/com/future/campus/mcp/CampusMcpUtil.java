package com.future.campus.mcp;

import cn.hutool.core.collection.CollUtil;
import dev.langchain4j.mcp.client.McpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CampusMcpUtil {


    @Autowired
    private List<CampusMcpClient> campusMcpClient;

    ///  获取所有mcp客户端
    public List<McpClient> getMcpClients(){
        if (CollUtil.isNotEmpty(campusMcpClient)){
            return campusMcpClient.stream().map(CampusMcpClient::getMcpClient).toList();
        }
        return null;
    }
}
