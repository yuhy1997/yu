package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.vo.CommentNewsVo;
import com.heima.model.article.vo.HotArticleVo;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.vos.SearchArticleVo;
import com.heima.model.wemedia.dtos.WmCommentStatusDto;
import com.heima.model.wemedia.dtos.WmNewsCommentsDto;
import org.apache.commons.net.nntp.Article;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper,ApArticle>implements ApArticleService {

    //小优化
    @Override
    public ResponseResult loadArticleList(ArticleHomeDto dtos, short type) {
        //1.参数校验 -- 分页size
        if(dtos.getSize() == null || dtos.getSize()<=0){
            //初始化size
            dtos.setSize(ArticleConstants.DEFAULT_SIZE);
        }
        //2.如果没有时间参数，那么代表我需要显示今天得新闻
        if(type != ArticleConstants.LOADTYPE_LOAD_MORE && type != ArticleConstants.LOADTYPE_LOAD_NEW){ //首页默认新闻（当天）
            type = ArticleConstants.LOADTYPE_LOAD_MORE; //查询更多得新闻
            //初始化查询新闻得时间
            dtos.setMinBehotTime(new Date()); //设置最小时间，代表获得今天更多得新闻
        }
        //查询mapper 获得最新或者更多得文章数据，然后返回给前端即可
        List<ApArticle> apArticles = apArticleMapper.loadArticleList(dtos, type);
        return ResponseResult.okResult(apArticles);
    }


    @Autowired
    private ApArticleMapper apArticleMapper;


    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    @Autowired
    private ArticleFreemarkerService articleFreemarkerService;


    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    /**
     * 保存或者修改文章代码
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        //1.检查参数
        if(null == dto){//参数有问题
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto,apArticle);
        //2.判断是否存在id
        if(dto.getId() == null){
            //2.1 不存在id  保存  文章  文章配置  文章内容
            //2.1.1 保存文章到数据库
            //补全数据
            apArticle.setCreatedTime(new Date());
            apArticleMapper.insert(apArticle);
            //2.1.2 保存文章配置
            ApArticleConfig apArticleConfig = new ApArticleConfig();
            apArticleConfig.setArticleId(apArticle.getId());
            apArticleConfigMapper.insert(apArticleConfig);
            //2.1.3 保存文章内容
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(apArticleContent);

        }else{
            //2.2 存在id   修改  文章  文章内容
            //介绍：已经发布过的文章，只能修改内容
            //2.2.1 直接根据文章ID修改文章的内容
            //update aparticlecontent set content = #{content} where article_id = #{article_id}
            LambdaUpdateWrapper<ApArticleContent> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ApArticleContent::getArticleId,apArticle.getId());
            wrapper.set(ApArticleContent::getContent,dto.getContent());
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContentMapper.update(apArticleContent,wrapper);
        }
        //3.结果返回  文章的id
        //todo 4.生成这篇审核成功的文章的静态页面
        articleFreemarkerService.buildArticleToMinIO(apArticle,dto.getContent());

        //todo 5.文章审核通过保存到mysql 数据库  把新发布的文章添加到es 里面
        //使用kafka 把输入发送给搜索服务添加到es 中
        //发送消息，创建索引
        createArticleESIndex(apArticle,dto.getContent(),apArticle.getStaticUrl());
        return ResponseResult.okResult(apArticle.getId());
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 优化文章查询接口
     * @param dtos  查询条件
     * @param type  定义一个值 type   1 = 查询更多   2 = 查询最新
     * @param firstPage true 代表首页（查询热点文章）  false（展示正常文章）
     * @return
     */
    @Override
    public ResponseResult loadArticleList2(ArticleHomeDto dtos, short type, boolean firstPage) {
        //1. 判断 firstPage true  查询热点文章代表首页
        if(firstPage){
            String redisKey = ArticleConstants.HOT_ARTICLE_PAGE + dtos.getTag();
            String str = redisTemplate.opsForValue().get(redisKey);
            //转换成数据对象
            List<HotArticleVo> articleVos = JSON.parseArray(str, HotArticleVo.class);

            if(articleVos != null || articleVos.size()>0){
                return ResponseResult.okResult(articleVos);
            }
        }
        //2. 判断 firstPage false   查询数据
        return loadArticleList(dtos,type);
    }

    /**
     * 根据作者ID查询作者文章信息
     * @param
     * @return
     */
    @Override
    public ResponseResult findByAuthorId(WmNewsCommentsDto dto) {
        //1.校验参数
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.查询数据库
        LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApArticle::getAuthorId,dto.getAuthorId());
        wrapper.orderByDesc(ApArticle::getPublishTime);
        //2.1 分页查询
        Page<ApArticle> page = apArticleMapper.selectPage(new Page<>(dto.getPage(),dto.getSize()),wrapper);
        //2.2 获得分页查询到的数据
        List<ApArticle> apArticles = page.getRecords();
        //2.3 返回值封装对象
        List<CommentNewsVo> commentNewsVos = new ArrayList<>();
        //3.便利所有文章。查询文章的状态
        for (ApArticle apArticle : apArticles) {
            CommentNewsVo cnv = new CommentNewsVo();
            cnv.setId(apArticle.getId());
            cnv.setCreatedTime(apArticle.getCreatedTime());
            cnv.setTitle(apArticle.getTitle());
            cnv.setComments(apArticle.getComment());
            //根据文章ID查询文章的评论状态
            LambdaQueryWrapper<ApArticleConfig> wrapperConfig = new LambdaQueryWrapper<>();
            wrapperConfig.eq(ApArticleConfig::getArticleId,apArticle.getId());
            ApArticleConfig apArticleConfig = apArticleConfigMapper.selectOne(wrapperConfig);
            cnv.setIsComment(apArticleConfig.getIsComment());
            commentNewsVos.add(cnv);
        }
        //4.封装返回对象
        PageResponseResult prr = new PageResponseResult((int)page.getCurrent(),(int)page.getSize(),(int)page.getTotal());
        prr.setData(commentNewsVos);
        prr.setCode(200);
        return prr;
    }

    /**
     * 根据文章ID修改文章的评论状态
     * @param dto
     * @return
     */
    @Override
    public ResponseResult updateStatusById(WmCommentStatusDto dto) {
        //1.校验参数
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.根据文章ID修改评论状态
        LambdaUpdateWrapper<ApArticleConfig> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ApArticleConfig::getArticleId,dto.getArticleId());
        wrapper.set(ApArticleConfig::getIsComment,dto.getOperation()==1?true:false);
        apArticleConfigMapper.update(null,wrapper);
        //3.返回结果
        //4.如果是关闭评论，把评论数量清0
        if(dto.getOperation() == 0){//关闭
            ApArticle apArticle = new ApArticle();
            apArticle.setId(dto.getArticleId());
            apArticle.setComment(0);
            apArticleMapper.updateById(apArticle);
        }
        return ResponseResult.okResult("执行成功");
    }

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    private void createArticleESIndex(ApArticle apArticle, String content, String staticUrl) {
        //封装需要传输的参数
        SearchArticleVo sav = new SearchArticleVo();
        BeanUtils.copyProperties(apArticle,sav);
        sav.setContent(content);
        sav.setStaticUrl(staticUrl);
        //使用kafka 发送消息给搜索服务即可
        kafkaTemplate.send("searchInsert", JSON.toJSONString(sav));
    }
}
