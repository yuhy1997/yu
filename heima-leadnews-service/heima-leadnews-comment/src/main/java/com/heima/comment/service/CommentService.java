package com.heima.comment.service;

import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.comment.dtos.CommentLikeDto;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.common.dtos.ResponseResult;

public interface CommentService {
    ResponseResult save(CommentSaveDto dto);

    ResponseResult load(CommentDto dto);

    ResponseResult like(CommentLikeDto dto);

    ResponseResult removeById(long id);
}
