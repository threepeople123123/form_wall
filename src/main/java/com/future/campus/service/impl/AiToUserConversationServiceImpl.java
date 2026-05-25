package com.future.campus.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.future.campus.entity.pojo.rdb.AiToUserConversationPoJo;
import com.future.campus.entity.pojo.rdb.UserPojo;
import com.future.campus.entity.request.SearchConversationRequest;
import com.future.campus.entity.response.SearchConversationResponse;
import com.future.campus.exception.FormWallException;
import com.future.campus.mapper.AiToUserConversationMapper;
import com.future.campus.service.AiToUserConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.typesense.api.Client;
import org.typesense.model.MultiSearchCollectionParameters;
import org.typesense.model.MultiSearchResult;
import org.typesense.model.MultiSearchResultItem;
import org.typesense.model.MultiSearchSearchesParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiToUserConversationServiceImpl extends ServiceImpl<AiToUserConversationMapper, AiToUserConversationPoJo> implements AiToUserConversationService {

    @Autowired
    private Client typesenseClient;

    /**
     * 搜索当前用户对话
     * @param searchConversationRequest 请求参数
     * @param user 当前用户
     * @return 对话列表
     */
    @Override
    public Page<SearchConversationResponse> searchConversation(SearchConversationRequest searchConversationRequest, UserPojo user) throws FormWallException {
        String collectionName = "source_vector";

        //  构建搜索关键词 (Query)
        // 如果有关键词，我们在 userMsg 和 botMsg 中进行搜索
        String queryKeyword = searchConversationRequest.getQuery() != null ? searchConversationRequest.getQuery() : "*";

        MultiSearchCollectionParameters parameters = new MultiSearchCollectionParameters();
        parameters.setQ("*");

        // 分页设置
        parameters.setPerPage(10);parameters.setCollection(collectionName);

        MultiSearchSearchesParameter searchParameters = new MultiSearchSearchesParameter();
        searchParameters.setSearches(List.of(parameters));

        try {
            MultiSearchResult multiSearchResult = typesenseClient.multiSearch.perform(searchParameters, Map.of());

            List<SearchConversationResponse> responseList = new ArrayList<>();
            long totalFound = 0;

            if (multiSearchResult.getResults() != null && !multiSearchResult.getResults().isEmpty()) {
                MultiSearchResultItem resultItem = multiSearchResult.getResults().get(0);
                totalFound = resultItem.getFound();

                if (resultItem.getGroupedHits() != null) {
                    resultItem.getGroupedHits().forEach(group -> {
                        if (group.getHits() != null && !group.getHits().isEmpty()) {

                            Map<String, Object> document = group.getHits().get(0).getDocument();

                            String text = (String) document.getOrDefault("botMsg", "");

                            Map<String, String> metadata = new HashMap<>();
                            document.forEach((k, v) -> metadata.put(k, String.valueOf(v)));

//                            responseList.add(Content.from(text));
                        }
                    });
                }
            }

            // 封装为 MyBatis-Plus 的 Page 对象返回
            Page<SearchConversationResponse> pageResult = new Page<>(searchConversationRequest.getPageNum(), searchConversationRequest.getPageSize());
            pageResult.setRecords(responseList);
            pageResult.setTotal(totalFound);
            return pageResult;

        } catch (Exception e) {
            log.error("TypeSense 搜索失败", e);
            throw new FormWallException("TypeSense 搜索失败",500);
        }
    }
}
