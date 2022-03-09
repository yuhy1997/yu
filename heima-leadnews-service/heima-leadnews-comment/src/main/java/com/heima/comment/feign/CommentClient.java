package com.heima.comment.feign;

import com.heima.apis.comment.ICommentClient;
import com.heima.comment.service.CommentService;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentClient implements ICommentClient {


    @Autowired
    private CommentService commentService;

    /**
     * 根据文章ID删除文章的评论信息
     * @param id
     * @return
     */
    @DeleteMapping("/api/v1/comment/removeById/{id}")
    @Override
    public ResponseResult removeById(@PathVariable("id")long id) {



        return commentService.removeById(id);
    }
}
