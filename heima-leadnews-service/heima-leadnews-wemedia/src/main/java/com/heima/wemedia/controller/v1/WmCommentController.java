package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmCommentStatusDto;
import com.heima.model.wemedia.dtos.WmNewsCommentsDto;
import com.heima.wemedia.service.WmCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作为管理评论相关操作
 */
@RestController
@RequestMapping("/api/v1/comment/manage")
public class WmCommentController {

    @Autowired
    private WmCommentService wmCommentService;


    /**
     * 查询展示文章的评论信息
     *   springmvc 请求参数类型有哪些？
     *      JSON    @RequestBody       body  -- json  -> {"":""}
     *      普通传参  @RequestParam("key")     localhost:8080?key=111&value=20
     *      restFul    @PathVariable("id")    localhost:8080/{id}
     */
    @PostMapping("/find_news_comments")
    public ResponseResult findNewsComments(@RequestBody WmNewsCommentsDto dto){

        return wmCommentService.findNewsComments(dto);
    }

    /**
     * 打开或者关闭评论功能
     * @return
     */
    @PostMapping("/update_comment_status")
    public ResponseResult updateCommentStatus(@RequestBody WmCommentStatusDto dto){


        return wmCommentService.updateCommentStatus(dto);
    }

}
