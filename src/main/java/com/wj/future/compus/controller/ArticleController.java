package com.wj.future.compus.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

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
        if (viewRange == 2){
            if (StrUtil.isBlank(schoolName) || StrUtil.isBlank(schoolId)){
                throw new FormWallException("请输入完整学校名称");
            }
        }

        // todo:使用es查询
        Criteria criteria = new Criteria();

        criteria.and(new Criteria("title")).contains(articleRequest.getQuery());
        criteria.and(new Criteria("content")).contains(articleRequest.getQuery()); // 姓名包含"张"
        criteria.and(new Criteria("schoolName").is(articleRequest.getSchoolName()));
        criteria.and(new Criteria("schoolId").is(articleRequest.getSchoolId()));
        criteria.and(new Criteria("viewRange").is(articleRequest.getViewRange()));

        Sort likeCountSort = Sort.by(Sort.Direction.DESC, "likeCount");
        Sort heatSort = Sort.by(Sort.Direction.DESC, "heat");
        Sort createTimeSort = Sort.by(Sort.Direction.DESC, "createTime");

        // 步骤2：构建Query对象
        Query query = new CriteriaQuery(criteria);
        query.addSort(createTimeSort);
        query.addSort(likeCountSort);
        query.addSort(heatSort);
        // 分页：第0页，每页10条
        query.setPageable(PageRequest.of(articleRequest.getPageNum(),articleRequest.getPageSize()));
        SearchHits<ArticleEsPojo> esPojoSearchHits = elasticsearchOperations.search(query, ArticleEsPojo.class);

        List<ArticleResponse> articleResponses = null;
        if (esPojoSearchHits.getTotalHits() > 0){
            List<ArticleEsPojo> articleEsPojoList= esPojoSearchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
            articleResponses = BeanUtil.copyToList(articleEsPojoList, ArticleResponse.class);

        }

        Page<ArticleResponse> articleResponsePage = new Page<>(articleRequest.getPageNum(),articleRequest.getPageSize(),esPojoSearchHits.getTotalHits());
        articleResponsePage.setRecords(articleResponses);
        return R.ok(articleResponsePage);
    }
}
