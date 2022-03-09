package com.heima.user.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserRelationDto;

public interface UserFollowService {
    ResponseResult follow(UserRelationDto dto);
}
