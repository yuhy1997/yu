package com.heima.behavior.controller.v1;


import com.heima.behavior.service.UnLikesBehaviorService;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UnLikesBehaviorController {

    @Autowired
    private UnLikesBehaviorService unLikesBehaviorService;

    @PostMapping("/un_likes_behavior")
    public ResponseResult unLikes(@RequestBody UnLikesBehaviorDto dto){

        return unLikesBehaviorService.unLikes(dto);
    }
}
