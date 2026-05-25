package com.future.campus.properties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("mcp")
@Component
@Data
public class McpProperties {


    @Schema(description = "高德地图调用mcp相关配置")
    private Amap amap;

    @Data
    public static class Amap{

        @Schema(description = "高德地图api-key")
        private String apiKeyUrl;
    }
}
