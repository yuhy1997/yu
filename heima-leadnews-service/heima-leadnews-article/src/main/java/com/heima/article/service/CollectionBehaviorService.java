package com.heima.article.service;

import com.heima.model.behavior.dtos.CollectionBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface CollectionBehaviorService {
    ResponseResult collection(CollectionBehaviorDto dto);
}
