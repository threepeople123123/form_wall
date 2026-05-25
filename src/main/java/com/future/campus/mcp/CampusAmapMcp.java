package com.future.campus.mcp;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.logging.DefaultMcpLogMessageHandler;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.springframework.stereotype.Component;

@Component
public class CampusAmapMcp implements CampusMcpClient{
    @Override
    public McpClient getMcpClient() {
        McpTransport transport = new HttpMcpTransport.Builder()
                .sseUrl("https://mcp.amap.com/sse?key=3c6e116912fe38c987139ef48caec428")
                .logRequests(true) // 打印请求
                .logResponses(true) // 打印响应
                .build();

        return new DefaultMcpClient.Builder()
                .key("AmapMcpClient") // 给客户端起个名字，方便多客户端时区分
                .logHandler(new DefaultMcpLogMessageHandler())
                .transport(transport)
                .build();
    }
}
