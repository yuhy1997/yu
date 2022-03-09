package com.heima.article.service;

import com.heima.model.mess.ArticleVisitStreamMess;

public interface HotArticleService {

    public void  HotArticle();

    /**
     * 更新分值
     * @param avsm
     */
    public void updateScore(ArticleVisitStreamMess avsm);

}
