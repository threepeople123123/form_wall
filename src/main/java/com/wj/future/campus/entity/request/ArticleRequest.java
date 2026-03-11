package com.wj.future.campus.entity.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * packageName com.wj.form.wall.entity.request
 *
 * @author wangj
 * @className ArticleRequest
 * @date 2026/3/2
 * @description 内容查询
 */
@Data
public class ArticleRequest {

    @Schema(description = "学校名称")
    private String schoolName;

    @Schema(description = "学校id")
    private String schoolId;

    @Schema(description = "查看范围,1:全部，2:学校")
    private int viewRange =1;

    @Schema(description = "标题，内容等")
    private String query;

    @Schema(description = "标签")
    private List<String> tag;

    @Schema(description = "页码")
    private int pageNum = 1;

    @Schema(description = "每页数量")
    private int pageSize = 20;
}
