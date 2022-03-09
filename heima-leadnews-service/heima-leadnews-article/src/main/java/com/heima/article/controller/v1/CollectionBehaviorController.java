package com.heima.article.controller.v1;

import com.heima.article.service.CollectionBehaviorService;
import com.heima.model.behavior.dtos.CollectionBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CollectionBehaviorController {

    @Autowired
    private CollectionBehaviorService  collectionBehaviorService;

    @PostMapping("/collection_behavior")
    public ResponseResult collection(@RequestBody CollectionBehaviorDto dto){


        return collectionBehaviorService.collection(dto);
    }
}
