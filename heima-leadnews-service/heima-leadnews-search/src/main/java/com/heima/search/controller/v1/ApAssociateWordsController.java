package com.heima.search.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.AssociateSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/associate")
public class ApAssociateWordsController {

    @Autowired
    private AssociateSearchService associateSearchService;

    /**
     *  搜索提醒词
     * @param dto
     * @return
     */
    @PostMapping("/search")
    public ResponseResult search(@RequestBody UserSearchDto dto){
        return associateSearchService.search(dto);
    }
}