package com.heima.apis.user;

import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("leadnews-user")
public interface IUserClient {

    @PostMapping("/api/v1/user/getUser/{id}")
    public ResponseResult getUser(@PathVariable("id") Integer id);

}
