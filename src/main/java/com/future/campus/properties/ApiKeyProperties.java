package com.future.campus.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "ai")
@Data
@Component
public class ApiKeyProperties {

    private Dashscope dashscope;

    private ZhiPu zhiPu;

    private Gateway gateway;

    /**
     * 网关配置
     */
    @Data
    public static class Gateway {
        private String url;

        private String  litellmMasterKey;
    }

    /**
     * 阿里百炼配置
     */
    @Data
    public static class Dashscope {
        private String qwen3_5_122b_a10b;

        private String qwen3_5_omni_plus_2026_03_15;

        private String dashscope_qwen_image_2_0;

        private String qwen3_5_plus;

        private String qianWenApiKey;

        private String tongyiXiaomiAnalysisPro;

        private String qianWenBaseUrl;
    }

    /**
     * 智谱配置
     */
    @Data
    public static class ZhiPu {
        private String zhiPuApiKey;

        private String zhiPuBaseUrl;

        private String GLMImage;
    }
}

