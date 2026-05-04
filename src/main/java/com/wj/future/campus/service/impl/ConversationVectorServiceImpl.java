package com.wj.future.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.future.campus.entity.pojo.rdb.ConversationVectorPojo;
import com.wj.future.campus.mapper.ConversationVectorMapper;
import com.wj.future.campus.service.ConversationVectorService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wj
 */
@Service
public class ConversationVectorServiceImpl extends ServiceImpl<ConversationVectorMapper, ConversationVectorPojo> implements ConversationVectorService {

    /**
     * 根据用户向量查询最相似的对话记录
     *
     * @param userVector 用户查询向量
     * @param limit 返回结果数量
     * @return 最相似的对话记录列表 (包含 botVector)
     */
    @Override
    public List<ConversationVectorPojo> findMostSimilarByUserVector(List<Double> userVector, int limit) {

        String vectorStr = vectorListToString(userVector);

        return getBaseMapper().selectMostSimilar(vectorStr, limit);
    }
    
    /**
     * 按学校 ID 查询所有向量记录 (使用 MyBatis-Plus 单表查询)
     *
     * @param schoolId 学校 ID
     * @return 向量记录列表
     */
    @Override
    public List<ConversationVectorPojo> findBySchoolId(String schoolId) {
        // 使用 LambdaQueryWrapper 构建类型安全的查询
        LambdaQueryWrapper<ConversationVectorPojo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConversationVectorPojo::getSchoolId, schoolId);
        
        return list(queryWrapper);
    }
    
    /**
     * 查询所有记录 (使用 MyBatis-Plus list 方法)
     *
     * @return 所有向量记录
     */
    @Override
    public List<ConversationVectorPojo> findAll() {
        return list();
    }
    
    /**
     * 将向量列表转换为 PostgreSQL vector 类型的字符串格式
     * 
     * @param vector 向量列表
     * @return 字符串格式，如 "[0.1,0.2,0.3]"
     */
    private String vectorListToString(List<Double> vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            sb.append(vector.get(i));
            if (i < vector.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
