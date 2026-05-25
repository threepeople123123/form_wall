package com.future.campus.mcp;

import com.future.campus.properties.McpProperties;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.logging.DefaultMcpLogMessageHandler;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CampusAmapMcp implements CampusMcpClient{

    @Autowired
    private McpProperties mcpProperties;

    @Override
    public McpClient getMcpClient() {
        McpTransport transport = new HttpMcpTransport.Builder()
                .sseUrl(mcpProperties.getAmap().getApiKeyUrl())
                .logRequests(true)
                .logResponses(true)
                .build();

        return new DefaultMcpClient.Builder()
                .key("AmapMcpClient") // 给客户端起个名字，方便多客户端时区分
                .logHandler(new DefaultMcpLogMessageHandler())
                .transport(transport)
                .build();
    }
}
