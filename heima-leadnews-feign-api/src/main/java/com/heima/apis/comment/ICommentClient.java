package com.heima.apis.comment;

import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("leadnews-comment")
public interface ICommentClient {
    /**
     * 根据文章ID删除该文章的所有评论
     */
    @DeleteMapping("/api/v1/comment/removeById/{id}")
    public ResponseResult removeById(@PathVariable("id") long id);
}
