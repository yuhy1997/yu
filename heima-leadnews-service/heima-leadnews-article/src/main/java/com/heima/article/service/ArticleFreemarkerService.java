package com.heima.article.service;

import com.heima.model.article.pojos.ApArticle;

public interface ArticleFreemarkerService {

    /**
     * 生成静态文件上传到minIO中
     * @param apArticle  要生成文章的基本信息
     * @param content 要生成文章的内容
     */
    public void buildArticleToMinIO(ApArticle apArticle,String content);
}