package com.heima.behavior.controller.v1;

import com.heima.behavior.service.LikeService;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 点赞行为接口
 */
@RestController
@RequestMapping("/api/v1")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/likes_behavior")
    public ResponseResult like(@RequestBody LikesBehaviorDto dto){

        return likeService.like(dto);
    }

}
