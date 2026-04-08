package com.wj.future.campus.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wj.future.campus.annotation.AuthIsLogin;
import com.wj.future.campus.entity.es.po.ArticleEsPojo;
import com.wj.future.campus.entity.pojo.ArticlePojo;
import com.wj.future.campus.entity.pojo.FilePojo;
import com.wj.future.campus.entity.pojo.UserPojo;
import com.wj.future.campus.entity.request.ArticleRequest;
import com.wj.future.campus.entity.request.SendArticleRequest;
import com.wj.future.campus.entity.response.ArticleResponse;
import com.wj.future.campus.exception.FormWallException;
import com.wj.future.campus.result.R;
import com.wj.future.campus.service.ArticleService;
import com.wj.future.campus.service.FileService;
import com.wj.future.campus.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.typesense.api.Client;
import org.typesense.model.SearchParameters;
import org.typesense.model.SearchResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private Client typesenseClient;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private FileService fileService;


    @AuthIsLogin
    @PostMapping("/publishArticle")
    public R<String> publish(@RequestBody SendArticleRequest sendArticleRequest , HttpServletRequest request) throws FormWallException {
        String article = sendArticleRequest.getArticle();
        List<String> photoIds = sendArticleRequest.getPhotoIds();
        int viewRange = sendArticleRequest.getViewRange();
        String title = sendArticleRequest.getTitle();

        if (StrUtil.isBlank(title) || title.length() > 100){
            throw new FormWallException("标题不能为空，并且要小于100字符");
        }
        if (StrUtil.isBlank(article)){
            throw new FormWallException("内容不能为空");
        }
        if (CollUtil.isNotEmpty(photoIds) && photoIds.size() > 9){
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

        boolean result = Boolean.TRUE.equals(transactionTemplate.execute(transactionStatus -> {
            articlePojo.setId(IdUtil.getSnowflakeNextId());
            articlePojo.setSendUserId(user.getUserId());
            articlePojo.setSendUserName(user.getUserName());
            articlePojo.setCreateTime(LocalDateTime.now());
            articlePojo.setUpdateTime(LocalDateTime.now());
            articlePojo.setPhotoUrl(JSONUtil.toJsonStr(photoIds));
            articlePojo.setContent(article);
            articlePojo.setViewRange(viewRange);
            boolean tableResult = articleService.save(articlePojo);

            if (CollUtil.isNotEmpty(photoIds)){
                // 更新
                LambdaUpdateWrapper<FilePojo> uw = new LambdaUpdateWrapper<>();
                uw.set(FilePojo::getCorrelationId,articlePojo.getId());
                uw.in(FilePojo::getId,photoIds);
                tableResult = fileService.update(uw);
            }
            if (tableResult) {

                ArticleEsPojo articleEsPojo = new ArticleEsPojo();
                articleEsPojo.setId(articlePojo.getId());
                articleEsPojo.setTitle(title);
                articleEsPojo.setContent(article);
                articleEsPojo.setPhotoUrl(articlePojo.getPhotoUrl());
                articleEsPojo.setCreateTime(LocalDateTime.now());
                articleEsPojo.setSendUserId(user.getUserId());
                articleEsPojo.setSendUserName(user.getUserName());
                articleEsPojo.setUpdateTime(LocalDateTime.now());
                articleEsPojo.setViewRange(viewRange);
                articleEsPojo.setSchoolId(user.getSchoolId());
                articleEsPojo.setSchoolName(user.getSchoolName());
                articleEsPojo.setLikeCount(0);
                articleEsPojo.setHeat(0);

                Map<String, Object> articleEsPojoToMap = BeanUtil.beanToMap(articleEsPojo);
                try {
                    typesenseClient.collections("article_index").documents().create(articleEsPojoToMap);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return tableResult;
        }));

        return  result ? R.okMsg("发布成功"):R.failure("发布失败");
    }

    /**
     * 查询列表
     * @param articleRequest 查询条件
     * @return 返回列表
     * @exception FormWallException 异常
     */
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
            SearchParameters searchParameters = new SearchParameters()
                    // 对应 ES 的 should (title / content)
                    .q(articleRequest.getQuery())
                    .queryBy("title,content")

                    // 对应 ES 的 filter
                    .filterBy(String.format("schoolName:=`%s` && schoolId:=`%s` && viewRange:=%d",
                            articleRequest.getSchoolName(),
                            articleRequest.getSchoolId(),
                            articleRequest.getViewRange()))

                    // 对应 ES 的 sort (注意：Typesense 中排序字段必须在 Schema 中预设为 sort: true)
                    .sortBy("createTime:desc,likeCount:desc,heat:desc")

                    // 分页 (Typesense 页码从 1 开始)
                    .page(articleRequest.getPageNum() + 1)
                    .perPage(articleRequest.getPageSize());

            // 2. 执行搜索

            SearchResult searchResult = typesenseClient.collections("article_index").documents().search(searchParameters);

            // 3. 处理结果映射 (Typesense 返回的是 Map<String, Object>)
            List<ArticleEsPojo> articleEsPojos = searchResult.getHits().stream()
                    .map(hit -> {
                        // Typesense SDK 会将 document 映射为 Map
                        return BeanUtil.fillBeanWithMap(hit.getDocument(), new ArticleEsPojo(), false);
                    })
                    .toList();

            // 4. 封装分页对象
            Page<ArticleResponse> articleResponsePage = new Page<>(
                    articleRequest.getPageNum(),
                    articleRequest.getPageSize(),
                    searchResult.getFound() // 总命中数
            );

            if (CollUtil.isNotEmpty(articleEsPojos)) {
                List<ArticleResponse> articleResponses = BeanUtil.copyToList(articleEsPojos, ArticleResponse.class);
                articleResponsePage.setRecords(articleResponses);
            }

            return R.ok(articleResponsePage);
        } catch (Exception e) {

            // 查询数据库
            throw new FormWallException("查询失败");
        }
    }
}
