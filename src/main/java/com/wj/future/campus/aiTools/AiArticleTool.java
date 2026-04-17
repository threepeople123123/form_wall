package com.wj.future.campus.aiTools;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wj.future.campus.controller.ArticleController;
import com.wj.future.campus.entity.request.ArticleRequest;
import com.wj.future.campus.entity.response.ArticleResponse;
import com.wj.future.campus.exception.FormWallException;
import com.wj.future.campus.result.R;
import dev.langchain4j.agent.tool.Tool;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AiArticleTool {

    @Autowired
    private ArticleController articleController; // 或者直接注入 Service

    @Tool("""
            搜索校园文章和帖子。
            当用户询问关于文章内容、查找帖子、搜索信息时使用此工具。
            
            参数说明:
            - query: 搜索关键词，可以是标题或内容的关键词
            - viewRange: 查看范围(1=全部, 2=指定学校)
            - schoolName: 学校名称(仅当viewRange=2时需要)
            - schoolId: 学校ID(仅当viewRange=2时需要)
            - pageNum: 页码，从1开始
            - pageSize: 每页数量，建议10-20
            
            返回: 分页的文章列表
            """)
    public R<Page<ArticleResponse>> searchArticles(
            @Schema(description = "搜索关键词，用于匹配文章标题或内容") String query,
            @Schema(description = "查看范围: 1=全部文章, 2=指定学校") int viewRange,
            @Schema(description = "学校名称，当viewRange=2时必填") String schoolName,
            @Schema(description = "学校ID，当viewRange=2时必填") String schoolId,
            @Schema(description = "页码，从1开始") int pageNum,
            @Schema(description = "每页数量，建议10-20") int pageSize
    ) throws FormWallException {
        log.info("🔧 AI 调用搜索工具 - query: {}, viewRange: {}, schoolName: {}, schoolId: {}, pageNum: {}, pageSize: {}",
                query, viewRange, schoolName, schoolId, pageNum, pageSize);
        
        ArticleRequest request = new ArticleRequest();
        request.setQuery(query);
        request.setViewRange(viewRange);
        request.setSchoolName(schoolName);
        request.setSchoolId(schoolId);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);

        // 直接调用你写好的逻辑
        R<Page<ArticleResponse>> result = articleController.pageList(request);
        
        log.info("🔧 搜索工具返回 - 总数: {}, 当前页数据量: {}", 
                result.getData() != null ? result.getData().getTotal() : 0,
                result.getData() != null ? result.getData().getRecords().size() : 0);
        
        return result;
    }

}
