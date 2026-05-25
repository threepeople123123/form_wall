package com.future.campus.controller;

import com.future.campus.result.R;
import com.future.campus.util.EmbeddingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Embedding 向量控制器
 * 提供文本向量化相关接口
 */
@RestController
@RequestMapping("/embedding")
@Tag(name = "Embedding向量服务", description = "文本向量化相关接口")
public class EmbeddingController {

    @Autowired
    private EmbeddingUtil embeddingUtil;

    /**
     * 将文本转换为向量
     * 
     * @param request 包含 text 字段
     * @return 向量数据
     */
    @PostMapping("/embed")
    @Operation(summary = "文本向量化", description = "将文本转换为向量表示")
    public R<Map<String, Object>> embed(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        
        if (text == null || text.isEmpty()) {
            return R.failure("文本不能为空");
        }

        try {
            // 获取向量（List<Double> 格式）
            List<Double> vector = embeddingUtil.embedToVector(text);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("text", text);
            result.put("vector", vector);
            result.put("dimension", vector.size());
            
            return R.ok(result);
        } catch (Exception e) {
            return R.failure("向量化失败: " + e.getMessage());
        }
    }

    /**
     * 计算两个文本的相似度
     * 
     * @param request 包含 text1 和 text2
     * @return 相似度分数
     */
    @PostMapping("/similarity")
    @Operation(summary = "计算文本相似度", description = "计算两段文本的余弦相似度")
    public R<Map<String, Object>> similarity(@RequestBody Map<String, String> request) {
        String text1 = request.get("text1");
        String text2 = request.get("text2");
        
        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
            return R.failure("文本不能为空");
        }

        try {
            // 分别向量化
            List<Double> vector1 = embeddingUtil.embedToVector(text1);
            List<Double> vector2 = embeddingUtil.embedToVector(text2);
            
            // 计算相似度
            double similarity = embeddingUtil.cosineSimilarity(vector1, vector2);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("text1", text1);
            result.put("text2", text2);
            result.put("similarity", similarity);
            result.put("description", getSimilarityDescription(similarity));
            
            return R.ok(result);
        } catch (Exception e) {
            return R.failure("相似度计算失败: " + e.getMessage());
        }
    }

    /**
     * 获取相似度描述
     */
    private String getSimilarityDescription(double similarity) {
        if (similarity >= 0.9) {
            return "非常相似";
        } else if (similarity >= 0.7) {
            return "比较相似";
        } else if (similarity >= 0.5) {
            return "中等相似";
        } else if (similarity >= 0.3) {
            return "略有相似";
        } else {
            return "不相似";
        }
    }
}
