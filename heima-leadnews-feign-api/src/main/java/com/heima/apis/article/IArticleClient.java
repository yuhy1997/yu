package com.heima.apis.article;

import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmCommentStatusDto;
import com.heima.model.wemedia.dtos.WmNewsCommentsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//从nacos 注册中心去获得这个服务名字访问地址
//leadnews-article = localhost:51802
@FeignClient(value = "leadnews-article")
public interface IArticleClient {

    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto) ;

    /**
     * 查询某个作者的所有文章
     */
    @PostMapping("/api/v1/article/findByAuthorId")
    public ResponseResult findByAuthorId(@RequestBody WmNewsCommentsDto dto);


    /**
     * 根据文章ID修改文章评论状态
     * 参数： 文章ID   修改
     */
    @PostMapping("/api/v1/article/updateStatusById")
    public ResponseResult updateStatusById(@RequestBody WmCommentStatusDto dto);

}