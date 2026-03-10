package com.wj.future.compus.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wj.future.compus.annotation.AuthIsLogin;
import com.wj.future.compus.entity.es.po.ArticleEsPojo;
import com.wj.future.compus.entity.pojo.ArticlePojo;
import com.wj.future.compus.entity.pojo.UserPojo;
import com.wj.future.compus.entity.request.ArticleRequest;
import com.wj.future.compus.entity.request.SendArticleRequest;
import com.wj.future.compus.entity.response.ArticleResponse;
import com.wj.future.compus.exception.FormWallException;
import com.wj.future.compus.result.R;
import com.wj.future.compus.service.ArticleService;
import com.wj.future.compus.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @AuthIsLogin
    @PostMapping("/publish")
    public R<String> publish(@RequestBody SendArticleRequest sendArticleRequest , HttpServletRequest request) throws FormWallException {
        String article = sendArticleRequest.getArticle();
        List<String> photoUrl = sendArticleRequest.getPhotoUrl();
        int viewRange = sendArticleRequest.getViewRange();
        String title = sendArticleRequest.getTitle();

        if (StrUtil.isBlank(title) || title.length() > 100){
            throw new FormWallException("标题不能为空，并且要小于100字符");
        }
        if (StrUtil.isBlank(article)){
            throw new FormWallException("内容不能为空");
        }
        if (CollUtil.isNotEmpty(photoUrl) && photoUrl.size() > 9){
            throw new FormWallException("最多不超过9张图片");
        }
        UserPojo user = userUtil.getUser(request);

        ArticlePojo articlePojo = new ArticlePojo();
        if (viewRange == 2){
            if (StrUtil.isBlank(user.getSchoolId())){
                throw new FormWallException("请先绑定学校");
            }else {
                articlePojo.setSchoolId(user.getSchoolId());
                articlePojo.setSchoolName(user.getSchoolName());
            }
        }
        articlePojo.setSendUserId(user.getUserId());
        articlePojo.setSendUserName(user.getUserName());
        articlePojo.setCreateTime(LocalDateTime.now());
        articlePojo.setUpdateTime(LocalDateTime.now());
        articlePojo.setPhotoUrl(JSONUtil.toJsonStr(photoUrl));
        articlePojo.setViewRange(viewRange);
        boolean save = articleService.save(articlePojo);
        return  save ? R.ok("发布成功"):R.failure("发布失败");
    }

    /**
     * 查询列表
     * @param articleRequest 查询条件
     * @return 返回列表
     * @exception FormWallException 异常
     */
    @AuthIsLogin
    @PostMapping("/pageList")
    public R<Page<ArticleResponse>> pageList(@RequestBody ArticleRequest articleRequest) throws FormWallException {
        int viewRange = articleRequest.getViewRange();
        String schoolId = articleRequest.getSchoolId();
        String schoolName = articleRequest.getSchoolName();
        if (viewRange == 2) {
            if (StrUtil.isBlank(schoolName) || StrUtil.isBlank(schoolId)) {
                throw new FormWallException("请输入完整学校名称");
            }
        }
        try {
            SearchResponse<ArticleEsPojo> esPojoSearchHits = elasticsearchClient.search(s -> s
                            .index("article_index")
                            .from(articleRequest.getPageNum() * articleRequest.getPageSize())
                            .size(articleRequest.getPageSize())

                            .query(q -> q
                                    .bool(b -> b

                                            // should：title / content
                                            .should(sh -> sh
                                                    .match(m -> m
                                                            .field("title")
                                                            .query(articleRequest.getQuery())
                                                    )
                                            )
                                            .should(sh -> sh
                                                    .match(m -> m
                                                            .field("content")
                                                            .query(articleRequest.getQuery())
                                                    )
                                            )

                                            // filter条件
                                            .filter(f -> f
                                                    .term(t -> t
                                                            .field("schoolName")
                                                            .value(articleRequest.getSchoolName())
                                                    )
                                            )
                                            .filter(f -> f
                                                    .term(t -> t
                                                            .field("schoolId")
                                                            .value(articleRequest.getSchoolId())
                                                    )
                                            )
                                            .filter(f -> f
                                                    .term(t -> t
                                                            .field("viewRange")
                                                            .value(articleRequest.getViewRange())
                                                    )
                                            )
                                    )
                            )

                            // 排序
                            .sort(sort -> sort
                                    .field(f -> f
                                            .field("createTime")
                                            .order(SortOrder.Desc)
                                    )
                            )
                            .sort(sort -> sort
                                    .field(f -> f
                                            .field("likeCount")
                                            .order(SortOrder.Desc)
                                    )
                            )
                            .sort(sort -> sort
                                    .field(f -> f
                                            .field("heat")
                                            .order(SortOrder.Desc)
                                    )
                            )

                    , ArticleEsPojo.class
            );
            List<ArticleEsPojo> articleEsPojos = esPojoSearchHits.hits()
                    .hits()
                    .stream()
                    .map(hit -> hit.source())
                    .toList();


            Page<ArticleResponse> articleResponsePage = new Page<>(articleRequest.getPageNum(),articleRequest.getPageSize(),esPojoSearchHits.hits().total().value());
            if (CollUtil.isNotEmpty(articleEsPojos)){
                List<ArticleResponse> articleResponses = BeanUtil.copyToList(articleEsPojos, ArticleResponse.class);
                articleResponsePage.setRecords(articleResponses);
            }
            return R.ok(articleResponsePage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
