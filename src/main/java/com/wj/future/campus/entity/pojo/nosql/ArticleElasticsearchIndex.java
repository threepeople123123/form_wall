package com.wj.future.campus.entity.pojo.nosql;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "article_index")
@Data
public class ArticleElasticsearchIndex {
    @Id
    @Schema(description ="主键%")
    private String id;

    @Schema(description = "标题")

    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "查看范围,1:全部，2:学校")
    private int viewRange;

    @Schema(description = "发送用户名称")
    private String sendUserName;

    @Schema(description = "发送用户id")
    private Long sendUserId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "图片地址")
    private List<String> photoUrl;

    @Schema(description = "点赞数量")
    private int likeCount;

    @Schema(description = "热度")
    private int heat;

    @Schema(description = "标签")
    private List<String> tag;

}
