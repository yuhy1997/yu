package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章查询相关controller
 */
@RestController
@RequestMapping("/api/v1/article")
public class ArticleHomeController {

    @Autowired
    private ApArticleService apArticleService;

    /**
     * 加载首页  0
     * @param dtos
     * @return
     */
    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleHomeDto dtos){
        return apArticleService.loadArticleList(dtos,ArticleConstants.LOADTYPE_LOAD);
    }

    /**
     * 加载更多  1
     * @param dtos
     * @return
     */
    @PostMapping("/loadmore")
    public ResponseResult loadMore(@RequestBody ArticleHomeDto dtos){
        return apArticleService.loadArticleList2(dtos,ArticleConstants.LOADTYPE_LOAD_MORE,false);
    }

    /**
     * 加载最新   2
     * @param dtos
     * @return
     */
    @PostMapping("/loadnew")
    public ResponseResult loadNew(@RequestBody ArticleHomeDto dtos){
        return apArticleService.loadArticleList2(dtos,ArticleConstants.LOADTYPE_LOAD_NEW,false);
    }


}



