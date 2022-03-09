package com.heima.wemedia.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmCommentStatusDto;
import com.heima.model.wemedia.dtos.WmNewsCommentsDto;

public interface WmCommentService {
    ResponseResult findNewsComments(WmNewsCommentsDto dto);

    ResponseResult updateCommentStatus(WmCommentStatusDto dto);
}
