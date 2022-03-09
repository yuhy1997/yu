package com.heima.behavior.service;

import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ReadBehaviorService {
    ResponseResult read(ReadBehaviorDto dto);

    public ResponseResult readToMongo(ReadBehaviorDto dto);
}
