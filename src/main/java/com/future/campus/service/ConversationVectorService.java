package com.future.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.future.campus.entity.pojo.rdb.ConversationVectorPojo;


import java.util.List;

public interface ConversationVectorService extends IService<ConversationVectorPojo> {
    
    /**
     * 根据用户向量查询最相似的对话记录
     * 
     * @param userVector 用户查询向量
     * @param limit 返回结果数量
     * @return 最相似的对话记录列表 (包含 botVector)
     */
    List<ConversationVectorPojo> findMostSimilarByUserVector(List<Double> userVector, int limit);
    
    /**
     * 按学校 ID 查询所有向量记录 (使用 MyBatis-Plus 单表查询)
     * 
     * @param schoolId 学校 ID
     * @return 向量记录列表
     */
    List<ConversationVectorPojo> findBySchoolId(String schoolId);
    
    /**
     * 查询所有记录 (使用 MyBatis-Plus list 方法)
     * 
     * @return 所有向量记录
     */
    List<ConversationVectorPojo> findAll();
}
