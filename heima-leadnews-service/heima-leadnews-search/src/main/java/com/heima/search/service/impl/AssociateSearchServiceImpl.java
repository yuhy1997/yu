package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.pojo.ApAssociateWords;
import com.heima.search.service.AssociateSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssociateSearchServiceImpl implements AssociateSearchService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ResponseResult search(UserSearchDto dto) {
        //1.校验参数
        if(dto == null || StringUtils.isEmpty(dto.getSearchWords())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.校验分页参数
        if(dto.getPageSize() < 0 || dto.getPageSize() > 10){
            dto.setPageSize(10);
        }
        //精确（左匹配）   多（全匹配）
        //3.使用mongo 进行分页条件模糊查询  /^.*?\黑.*/
        String parrent ="^"+dto.getSearchWords()+".*"; //难点
        Query query = new Query(Criteria.where("associateWords").regex(parrent));
        query.limit(dto.getPageSize());
        List<ApAssociateWords> apAssociateWords = mongoTemplate.find(query, ApAssociateWords.class);
        return ResponseResult.okResult(apAssociateWords);
    }
}
