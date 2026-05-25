package com.future.campus.entity.pojo.nosql;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.elasticsearch.annotations.DateFormat.date_hour_minute_second;

/**
 * packageName com.wj.future.campus.entity.es.po
 *
 * @author wangj
 * @className ArticleEsPojo
 * @date 2026/3/5
 * @description 文章es pojo
 */
@Document(indexName = "article")
@Data
public class ArticleIndex {
    @Id
    private String id;

    @Field(name = "title",type = FieldType.Text,analyzer = "ik_max_word")
    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    @Field(name = "content",type = FieldType.Text,analyzer = "ik_max_word")
    private String content;

    @Schema(description = "查看范围,1:全部，2:学校")
    @Field(name = "viewRange",type = FieldType.Keyword)
    private int viewRange;

    @Schema(description = "发送用户名称")
    @Field(name = "sendUserName",type = FieldType.Keyword)
    private String sendUserName;

    @Schema(description = "发送用户id")
    @Field(name = "sendUserId",type = FieldType.Keyword)
    private Long sendUserId;

    @Schema(description = "创建时间")
    @Field(name = "createTime",type = FieldType.Date,format = date_hour_minute_second,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "标签")
    @Field(name = "tag",type = FieldType.Keyword)
    private List<String> tag;

    @Schema(description = "更新时间")
    @Field(name = "updateTime",type = FieldType.Date,format = date_hour_minute_second,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "学校id")
    @Field(name = "schoolId",type = FieldType.Keyword)
    private String schoolId;

    @Schema(description = "学校名称")
    @Field(name = "schoolName",type = FieldType.Keyword)
    private String schoolName;

    @Schema(description = "图片地址")
    @Field(name = "photoUrl",type = FieldType.Keyword)
    private String photoUrl;

    @Schema(description = "点赞数")
    @Field(name = "likeCount",type = FieldType.Integer)
    private int likeCount;

    @Schema(description = "热度")
    @Field(name = "heat",type = FieldType.Integer)
    private int heat;
}
