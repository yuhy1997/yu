package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

public interface AssociateSearchService {


    ResponseResult search(UserSearchDto dto);
}