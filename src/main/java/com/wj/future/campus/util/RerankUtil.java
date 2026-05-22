package com.wj.future.campus.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wj.future.campus.entity.request.RerankRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RerankUtil {

    public static final Logger logger =  LoggerFactory.getLogger(RerankUtil.class);

    public List<String> rerank(String query,List<String> contentList,int total){

        /**
         * http://localhost:12435/v1/rerank \
         *   -H "Content-Type: application/json" \
         *   -d '{
         *     "query": "什么是苹果？",
         *     "documents": ["苹果是一种常见的水果。", "苹果公司发布了最新的 iPhone。"]
         *   }'
         * {"model":"model.gguf","object":"list","usage":{"prompt_tokens":165,"total_tokens":165},"results":[{"index":0,"relevance_score":0.9949808716773987},{"index":1,"relevance_score":0.025742139667272568}]}%
         */
        if (CollUtil.isNotEmpty(contentList)){
            RerankRequest request =  new RerankRequest();
            request.setQuery(query);
            request.setDocuments(contentList);
            String post = HttpUtil.post("http://localhost:12435/v1/rerank", JSONUtil.toJsonStr(request));
            JSONObject result = JSONUtil.parseObj(post);
            List<JSONObject> results = result.getBeanList("results", JSONObject.class);
            results = CollUtil.isNotEmpty(results) ? results.subList(0,total)  : null;
            List<String> rerankList = new ArrayList<>();
            for (JSONObject item : results) {
                Integer index = item.getInt("index");
                String s = contentList.get(index);

                rerankList.add(s);
            }
            return rerankList;
        }

        return null;

    }

}
