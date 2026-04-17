package com.wj.future.campus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.typesense.api.Client;
import org.typesense.resources.Node;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class TypesenseConfig {

    @Value("${typesense.agreement}")
    private String agreement;

    @Value("${typesense.host}")
    private String host;

    @Value("${typesense.port}")
    private String port;

    @Value("${typesense.apiKey}")
    private String apiKey;

    @Bean
    public Client typesenseClient() {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(agreement, host, port));

        // 配置参数：API Key，节点列表，连接超时时间
        org.typesense.api.Configuration configuration = new org.typesense.api.Configuration(
                nodes, 
                Duration.ofSeconds(2),
                apiKey
        );

        return new Client(configuration);
    }
}