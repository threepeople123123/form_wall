package com.future.campus.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.future.campus.checkLogin.AuthIsLogin;
import com.future.campus.entity.pojo.nosql.ArticleIndex;
import com.future.campus.entity.pojo.rdb.ArticlePojo;
import com.future.campus.entity.pojo.rdb.FilePojo;
import com.future.campus.entity.pojo.rdb.TagPojo;
import com.future.campus.entity.pojo.rdb.UserPojo;
import com.future.campus.entity.request.ArticleRequest;
import com.future.campus.entity.request.SendArticleRequest;
import com.future.campus.entity.response.ArticleResponse;
import com.future.campus.exception.FormWallException;
import com.future.campus.result.R;
import com.future.campus.service.ArticleService;
import com.future.campus.service.FileService;
import com.future.campus.util.UserUtil;
//import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.typesense.api.Client;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.future.campus.campusEnum.RedisEnum.ARTICLE_DETAIL;

@RestController
@RequestMapping("/article")
public class ArticleController {


    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(ArticleController.class);

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

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @AuthIsLogin
    @PostMapping("/publishArticle")
    public R<String> publish(@RequestBody SendArticleRequest sendArticleRequest , HttpServletRequest request) throws FormWallException {
        String content = sendArticleRequest.getContent();
        List<FilePojo> photoIds = sendArticleRequest.getImageUrls();
        int viewRange = sendArticleRequest.getViewRange();
        String title = sendArticleRequest.getTitle();
        List<TagPojo> tags = sendArticleRequest.getTags();


        if (StrUtil.isBlank(title) || title.length() > 100){
            throw new FormWallException("标题不能为空，并且要小于100字符");
        }
        if (StrUtil.isBlank(content)){
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
            articlePojo.setSendUserId(user.getId());
            articlePojo.setSendUserName(user.getName());
            articlePojo.setCreateTime(LocalDateTime.now());
            articlePojo.setTitle(title);
            articlePojo.setUpdateTime(LocalDateTime.now());
            articlePojo.setPhotoUrl(JSONUtil.toJsonStr(photoIds));
            articlePojo.setContent(content);
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

                ArticleIndex articleIndex = new ArticleIndex();
                articleIndex.setId(articlePojo.getId().toString());
                articleIndex.setTitle(title);
                articleIndex.setContent(content);
                articleIndex.setPhotoUrl(articlePojo.getPhotoUrl());
                articleIndex.setCreateTime(LocalDateTime.now());
                articleIndex.setSendUserId(user.getId());
                articleIndex.setSendUserName(user.getName());
                articleIndex.setUpdateTime(LocalDateTime.now());
                articleIndex.setViewRange(viewRange);
                articleIndex.setSchoolId(user.getSchoolId());
                articleIndex.setSchoolName(user.getSchoolName());
                articleIndex.setLikeCount(0);
                articleIndex.setHeat(0);
                articleIndex.setTag(tags.stream().map(TagPojo::getTagName).toList());
                articleIndex.setSchoolId("test");
                articleIndex.setSchoolName("test");

                Map<String, Object> articleIndexToMap = BeanUtil.beanToMap(articleIndex, new LinkedHashMap<>(),
                        CopyOptions.create().setFieldValueEditor((fieldName, fieldValue) -> {
                            // 如果字段值是 LocalDateTime 类型,将其转换为 Unix 时间戳(秒)
                            if (fieldValue instanceof LocalDateTime) {
                                // Typesense 要求 int64 类型的时间戳
                                return ((LocalDateTime) fieldValue).atZone(java.time.ZoneId.systemDefault()).toInstant().getEpochSecond();
                            }
                            return fieldValue;
                        })
                );
                try {
                    typesenseClient.collections("article_index").documents().create(articleIndexToMap);

                    // 塞入redis，设置一天过期时间
                    ArticleResponse articleResponse = BeanUtil.copyProperties(articlePojo, ArticleResponse.class);
                    articleResponse.setPhotoUrl(photoIds);
                    redisTemplate.opsForValue().set(ARTICLE_DETAIL.getKey()+":"+articlePojo.getId(),JSONUtil.toJsonStr(articleResponse),1, TimeUnit.DAYS);
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
//    @Tool("根据学校名称，学校id，查询范围，标题，内容，标签，页码，每页数量进行查询")
    public R<Page<ArticleResponse>> pageList(@RequestBody ArticleRequest articleRequest) throws FormWallException {
        int viewRange = articleRequest.getViewRange();
        String schoolId = articleRequest.getSchoolId();
        String schoolName = articleRequest.getSchoolName();
        if (viewRange == 2) {
            if (StrUtil.isBlank(schoolName) || StrUtil.isBlank(schoolId)) {
                throw new FormWallException("请输入完整学校名称");
            }
        }
       /* try {
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
            List<ArticleEsPojo> articleIndexs = searchResult.getHits().stream()
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

            if (CollUtil.isNotEmpty(articleIndexs)) {
                List<ArticleResponse> articleResponses = BeanUtil.copyToList(articleIndexs, ArticleResponse.class);
                articleResponsePage.setRecords(articleResponses);
            }

            return R.ok(articleResponsePage);
        } catch (Exception e) {*/

            // 查询数据库
            try {
                LambdaQueryWrapper<ArticlePojo> qw = new LambdaQueryWrapper<>();
                qw.and(StrUtil.isNotBlank(articleRequest.getQuery()),queryWrapper ->
                        queryWrapper.like(ArticlePojo::getTitle, articleRequest.getQuery())
                                .or()
                                .like(ArticlePojo::getContent, articleRequest.getQuery()));

                qw.eq(ArticlePojo::getViewRange, String.valueOf(viewRange));
                qw.eq(StrUtil.isNotBlank(schoolId),ArticlePojo::getSchoolId, schoolId);
                qw.eq(StrUtil.isNotBlank(schoolName),ArticlePojo::getSchoolName, schoolName);
                Page<ArticlePojo> articlePojoPage = articleService.page(new Page<>(articleRequest.getPageNum(), articleRequest.getPageSize()), qw);
                List<ArticlePojo> records = articlePojoPage.getRecords();

                Page<ArticleResponse> articleResponsePage = new Page<>(
                        articleRequest.getPageNum(),
                        articleRequest.getPageSize(),
                        articlePojoPage.getTotal() // 总命中数
                );

                if (CollUtil.isNotEmpty(records)){
                    List<ArticleResponse> articleResponses = BeanUtil.copyToList(records, ArticleResponse.class);
                    articleResponsePage.setRecords(articleResponses);
                }
                return R.ok(articleResponsePage);
            }catch (Exception exception){
                logger.error("报错信息：{}",exception);
                throw new FormWallException("查询失败");
            }
//        }
    }

    /*
     查询文章详情
     */
    @GetMapping("/getById/{id}")
    public R<ArticleResponse> getById(@PathVariable Long id) throws FormWallException {
        String redisDetail = (String)redisTemplate.opsForValue().get(ARTICLE_DETAIL.getKey()+ ":"+ id);
        if (StrUtil.isNotBlank(redisDetail)){
            ArticleResponse articleResponse = JSONUtil.toBean(redisDetail, ArticleResponse.class);
            return R.ok(articleResponse);
        }

        // 增加
        ArticlePojo articlePojo = articleService.getById(id);
        if (ObjectUtil.isEmpty(articlePojo)){
            throw new FormWallException("文章不存在");
        }
        ArticleResponse articleResponse = BeanUtil.copyProperties(articlePojo, ArticleResponse.class);
        // 塞入redis
        redisTemplate.opsForValue().set(ARTICLE_DETAIL.getKey()+":"+articlePojo.getId(),JSONUtil.toJsonStr(articleResponse),1, TimeUnit.DAYS);
        return R.ok(articleResponse);
    }

    /**
     * 查询用户发布的文章
     * @param articleRequest 查询条件
     * @param request 请求信息
     * @return 文章列白哦
     * @throws FormWallException 异常
     */
    @AuthIsLogin
    @PostMapping("/pageListByUser")
    public R<Page<ArticleResponse>> pageList(@RequestBody ArticleRequest articleRequest,HttpServletRequest request) throws FormWallException {

        UserPojo userPojo = userUtil.getUser(request);

        LambdaQueryWrapper<ArticlePojo> qw = new LambdaQueryWrapper<>();
        qw.eq(ArticlePojo::getSendUserId,userPojo.getId());
        qw.and(StrUtil.isNotBlank(articleRequest.getQuery()),queryWrapper ->
                queryWrapper.like(ArticlePojo::getTitle, articleRequest.getQuery())
                        .or()
                        .like(ArticlePojo::getContent, articleRequest.getQuery()));
        Page<ArticlePojo> articlePojoPage = articleService.page(new Page<>(articleRequest.getPageNum(), articleRequest.getPageSize()), qw);
        List<ArticlePojo> records = articlePojoPage.getRecords();

        Page<ArticleResponse> articleResponsePage = new Page<>(
                articleRequest.getPageNum(),
                articleRequest.getPageSize(),
                articlePojoPage.getTotal() // 总命中数
        );

        if (CollUtil.isNotEmpty(records)){
            List<ArticleResponse> articleResponses = BeanUtil.copyToList(records, ArticleResponse.class);
            articleResponsePage.setRecords(articleResponses);
        }
        return R.ok(articleResponsePage);
    }
}
