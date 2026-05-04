package com.wj.future.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.future.campus.entity.pojo.rdb.AiToUserConversationPoJo;
import com.wj.future.campus.mapper.AiToUserConversationMapper;
import com.wj.future.campus.service.AiToUserConversationService;
import org.springframework.stereotype.Service;

@Service
public class AiToUserConversationServiceImpl extends ServiceImpl<AiToUserConversationMapper, AiToUserConversationPoJo> implements AiToUserConversationService {
}
