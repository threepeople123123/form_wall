package com.wj.future.campus.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "ai")
@Data
@Component
public class ApiKeyProperties {

    private String qianWenApiKey;

    private String zhiPuApiKey;

    private String tongyiXiaomiAnalysisPro;

    private String qwen3_5_122b_a10b;

    private String qianWenBaseUrl;

    private String qwen3_5_plus;
}
