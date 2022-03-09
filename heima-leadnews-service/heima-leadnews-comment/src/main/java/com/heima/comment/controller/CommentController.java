package com.heima.comment.controller;

import com.heima.comment.service.CommentService;
import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.comment.dtos.CommentLikeDto;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/comment")
public class CommentController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CommentService commentService;

    /**
     * 保存评论
     */
    @PostMapping("/save")
    public ResponseResult save(@RequestBody CommentSaveDto dto){

        return commentService.save(dto);
    }

    /**
     * 查询评论列表
     */
    @PostMapping("/load")
    public ResponseResult load(@RequestBody CommentDto dto){
        Long increment = redisTemplate.opsForValue().increment(dto.getArticleId()+dto.getIndex()+"");
        redisTemplate.expire(dto.getArticleId()+dto.getIndex()+"",1,TimeUnit.SECONDS);
        if(increment > 1){
            return ResponseResult.okResult(null);
        }
        return commentService.load(dto);
    }


    /**
     * 点赞和取消点赞
     */
    @PostMapping("/like")
    public ResponseResult like(@RequestBody CommentLikeDto dto){


        return commentService.like(dto);
    }

}
