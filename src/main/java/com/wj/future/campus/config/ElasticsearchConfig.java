package com.wj.future.campus.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {
//
//    @Value("${spring.elasticsearch.rest.uris}")
//    private String esUris;
//
//    @Bean
//    public ElasticsearchClient elasticsearchClient() {
//        String uri = esUris.replace("http://", "").replace("https://", "");
//        String[] parts = uri.split(":");
//        String host = parts[0];
//        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
//
//        RestClient restClient = RestClient.builder(
//                        new HttpHost(host, port, "http"))
//                .build();
//
//        RestClientTransport transport = new RestClientTransport(
//                restClient, new JacksonJsonpMapper());
//
//        return new ElasticsearchClient(transport);
//    }
}