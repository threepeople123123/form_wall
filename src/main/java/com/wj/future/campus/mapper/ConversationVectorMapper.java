package com.wj.future.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wj.future.campus.entity.pojo.rdb.ConversationVectorPojo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConversationVectorMapper extends BaseMapper<ConversationVectorPojo> {

    /**
     * 根据用户向量查询最相似的记录 (使用余弦相似度)
     * PostgreSQL vector 类型使用 <=> 运算符计算距离
     * 
     * @param userVector 用户查询向量 (数组格式)
     * @param limit 返回结果数量
     * @return 最相似的对话记录列表
     */
    List<ConversationVectorPojo> selectMostSimilar(@Param("userVector") String userVector, @Param("limit") int limit);
    
    /**
     * 使用 MyBatis-Plus 原生方式查询 (按学校 ID 过滤)
     * 
     * @param schoolId 学校 ID
     * @return 所有向量记录
     */
    default List<ConversationVectorPojo> selectBySchoolId(String schoolId) {
        return null; // 由 Service 层使用 QueryWrapper 实现
    }
}
