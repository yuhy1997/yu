package com.heima.wemedia.service.impl;

import com.heima.apis.article.IArticleClient;
import com.heima.apis.comment.ICommentClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmCommentStatusDto;
import com.heima.model.wemedia.dtos.WmNewsCommentsDto;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.service.WmCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WmCommentServiceImpl implements WmCommentService {


    @Autowired
    private IArticleClient iArticleClient;

    @Override
    public ResponseResult findNewsComments(WmNewsCommentsDto dto) {
        //1.校验参数
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.校验分页参数
        dto.checkParam();

        WmUser user = WmThreadLocalUtil.getUser();
        if(user == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        dto.setAuthorId(user.getId());
        //3.查询所有已经发布的文章 （倒叙）   查询article 服务
        ResponseResult responseResult = iArticleClient.findByAuthorId(dto);

        if(responseResult == null || responseResult.getCode() != 200){
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }

        return responseResult;
    }

    /**
     * 打开或者关闭评论功能
     * @param dto
     * @return
     */
    @Override
    public ResponseResult updateCommentStatus(WmCommentStatusDto dto) {
        //1.校验参数
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.远程调用文章微服务修改文章的状态
        ResponseResult responseResult = iArticleClient.updateStatusById(dto);

        if(responseResult == null || responseResult.getCode() != 200 ){
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }
        if(dto.getOperation() == 0){//打开评论  ap_article_config  修改
            //- 打开评论，可以让任何用户评论该文章
            //- 关闭评论，不让任何用户评论该文章，关闭评论，则清除所有该文章的评论
            //删除该文章的所有评论
            ResponseResult responseResult1 = iCommentClient.removeById(dto.getArticleId());
            if(responseResult1 == null || responseResult1.getCode() != 200){
               return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
            }
        }

        //修改文章评论数为0

        return ResponseResult.okResult("操作成功");
    }

    @Autowired
    private ICommentClient iCommentClient;
}
