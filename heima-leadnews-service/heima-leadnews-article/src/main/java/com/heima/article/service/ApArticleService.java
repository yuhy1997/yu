package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmCommentStatusDto;
import com.heima.model.wemedia.dtos.WmNewsCommentsDto;

public interface ApArticleService extends IService<ApArticle> {

    /**
     * 文章首页查询功能接口
     * 定义一个值 type   1 = 查询更多   2 = 查询最新
     */
    public ResponseResult loadArticleList(ArticleHomeDto dtos , short type);


    ResponseResult saveArticle(ArticleDto dto);


    /**
     *
     * @param dtos  查询条件
     * @param type  定义一个值 type   1 = 查询更多   2 = 查询最新
     * @param firstPage true 代表首页（查询热点文章）  false（展示正常文章）
     * @return
     */
    public ResponseResult loadArticleList2(ArticleHomeDto dtos , short type ,boolean firstPage);

    ResponseResult findByAuthorId(WmNewsCommentsDto dto);

    ResponseResult updateStatusById(WmCommentStatusDto dto);
}
