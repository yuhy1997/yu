package com.heima.article.feign;

import com.heima.apis.article.IArticleClient;
import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmCommentStatusDto;
import com.heima.model.wemedia.dtos.WmNewsCommentsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticleClient implements IArticleClient {

    @Autowired
    private ApArticleService apArticleService;

    @Override
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto) {

        return apArticleService.saveArticle(dto);
    }

    @PostMapping("/api/v1/article/findByAuthorId")
    @Override
    public ResponseResult findByAuthorId(@RequestBody WmNewsCommentsDto dto) {



        return apArticleService.findByAuthorId(dto);
    }

    /**
     * 修改文章配置表
     * @param dto
     * @return
     */
    @Override
    @PostMapping("/api/v1/article/updateStatusById")
    public ResponseResult updateStatusById(@RequestBody WmCommentStatusDto dto) {

        return apArticleService.updateStatusById(dto);
    }

}
