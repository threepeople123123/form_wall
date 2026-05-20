package com.wj.future.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wj.future.campus.entity.pojo.rdb.AiToUserConversationPoJo;
import com.wj.future.campus.entity.pojo.rdb.UserPojo;
import com.wj.future.campus.entity.request.SearchConversationRequest;
import com.wj.future.campus.entity.response.SearchConversationResponse;
import com.wj.future.campus.exception.FormWallException;

public interface AiToUserConversationService  extends IService<AiToUserConversationPoJo> {

    /**
     * 搜索当前用户对话
     * @param searchConversationRequest 请求参数
     * @param user 当前用户
     * @return 对话列表
     */
    Page<SearchConversationResponse> searchConversation(SearchConversationRequest searchConversationRequest, UserPojo user) throws FormWallException;

}
