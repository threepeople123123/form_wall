package com.wj.future.campus.entity.request;

import com.wj.future.campus.entity.pojo.rdb.FilePojo;
import com.wj.future.campus.entity.pojo.rdb.TagPojo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SendArticleRequest {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "查看范围,1:全部，2:学校")
    private int viewRange = 1;

    @Schema(description = "标签列表")
    private List<TagPojo> tags;

    @Schema(description = "图片地址")
    private List<FilePojo> imageUrls;
}
