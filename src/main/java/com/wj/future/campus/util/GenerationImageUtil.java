package com.wj.future.campus.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wj.future.campus.properties.ApiKeyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class GenerationImageUtil {
    public static final String IMAGE_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";

    @Autowired
    private ApiKeyProperties apiKeyProperties;

    /**
     * 生成图片
     * @param prompt 图片描述文本
     * @return 生成的图片URL或相关信息
     */
    public String generationImage(String prompt) {
        return generationImage(prompt, "1120*1440", false);
    }

    /**
     * 生成图片
     * @param prompt 图片描述文本
     * @param size 图片尺寸，如 "1120*1440"
     * @param promptExtend 是否扩展提示词
     * @return 生成的图片URL或相关信息
     */
    public String generationImage(String prompt, String size, boolean promptExtend) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            
            // model 设置
            requestBody.put("model", apiKeyProperties.getDashscope().getDashscope_qwen_image_2_0());
            
            // input 部分
            Map<String, Object> input = new HashMap<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            
            // content 数组
            Map<String, String> contentItem = new HashMap<>();
            contentItem.put("text", prompt);
            message.put("content", new Object[]{contentItem});
            
            input.put("messages", new Object[]{message});
            requestBody.put("input", input);
            
            // parameters 部分
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("prompt_extend", promptExtend);
            parameters.put("size", size);
            requestBody.put("parameters", parameters);
            
            // 发送HTTP请求
            HttpResponse response = HttpRequest.post(IMAGE_URL)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKeyProperties.getDashscope().getQianWenApiKey())
                    .body(JSONUtil.toJsonStr(requestBody))
                    .timeout(30000) // 30秒超时
                    .execute();
            
            if (response.isOk()) {
                String responseBody = response.body();
                log.info("图片生成成功: {}", responseBody);
                
                // 解析响应获取图片URL
                // 实际返回结构:
                // {
                //   "output": {
                //     "choices": [{
                //       "message": {
                //         "content": [{
                //           "image": "https://..."
                //         }]
                //       }
                //     }]
                //   }
                // }
                JSONObject jsonObject = JSONUtil.parseObj(responseBody);
                JSONObject output = jsonObject.getJSONObject("output");
                
                if (output != null) {
                    // 获取 choices 数组
                    JSONArray choices = output.getJSONArray("choices");
                    if (choices != null && !choices.isEmpty()) {
                        // 获取第一个 choice
                        JSONObject firstChoice = choices.getJSONObject(0);
                        if (firstChoice != null) {
                            // 获取 message 对象
                            JSONObject messageObj = firstChoice.getJSONObject("message");
                            if (messageObj != null) {
                                // 获取 content 数组
                                JSONArray content = messageObj.getJSONArray("content");
                                if (content != null && !content.isEmpty()) {
                                    // 获取第一个 content 项
                                    JSONObject firstContent = content.getJSONObject(0);
                                    if (firstContent != null) {
                                        // 获取 image URL
                                        String imageUrl = firstContent.getStr("image");
                                        if (imageUrl != null && !imageUrl.isEmpty()) {
                                            log.info("图片生成成功，URL: {}", imageUrl);
                                            return imageUrl;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 如果无法解析出图片URL，返回原始响应
                log.warn("无法从响应中解析图片URL，返回原始响应");
                return responseBody;
            } else {
                log.error("图片生成失败，状态码: {}, 响应: {}", response.getStatus(), response.body());
                throw new RuntimeException("图片生成失败: " + response.body());
            }
            
        } catch (Exception e) {
            log.error("调用图片生成API异常", e);
            throw new RuntimeException("调用图片生成API异常: " + e.getMessage(), e);
        }
    }
}
