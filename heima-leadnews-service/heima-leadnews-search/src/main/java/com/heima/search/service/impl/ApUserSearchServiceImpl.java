package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.pojo.ApUserSearch;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.thread.ApUserThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ApUserSearchServiceImpl implements ApUserSearchService {

    @Autowired
    private MongoTemplate mongoTemplate;



    @Async
    @Override
    public void insert(String keyword, Integer userId) {
        //1.查询当前用户的搜索关键词
        Query query = Query.query(Criteria.where("userId").is(userId).and("keyword").is(keyword));
        ApUserSearch apUserSearch = mongoTemplate.findOne(query, ApUserSearch.class);

        //2.判断数据是否存在，如果存在，更新时间
        if(apUserSearch != null){
            apUserSearch.setCreatedTime(new Date());
            mongoTemplate.save(apUserSearch);
            return;
        }

        //3.不存在 判断数量是否达标
        apUserSearch = new ApUserSearch();
        apUserSearch.setUserId(userId);
        apUserSearch.setKeyword(keyword);
        apUserSearch.setCreatedTime(new Date());

        Query query1 = Query.query(Criteria.where("userId").is(userId));
        query1.with(Sort.by(Sort.Direction.DESC,"createdTime"));
        List<ApUserSearch> apUserSearchList = mongoTemplate.find(query1, ApUserSearch.class);

        //4.判断数量是否达标
        if(apUserSearchList == null || apUserSearchList.size() < 10){
            mongoTemplate.save(apUserSearch);
        }else {
            //最后i一个
            ApUserSearch lastUserSearch = apUserSearchList.get(apUserSearchList.size() - 1);
            //mongoTemplate.findAndReplace(Query.query(Criteria.where("id").is(lastUserSearch.getId())),apUserSearch);
            mongoTemplate.remove(lastUserSearch);
            mongoTemplate.save(apUserSearch);
        }

    }

    /**
     * 从mongo中查询出这个用户的记录即可
     * @return
     */
    @Override
    public ResponseResult load() {
        //1.获得用户的ID
        ApUser user = ApUserThreadLocalUtil.getUser();
        if(user == null || user.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //2.根据用户ID 去Mongo 中查询即可
        Query query1 = Query.query(Criteria.where("userId").is(user.getId()));
        query1.with(Sort.by(Sort.Direction.DESC,"createdTime"));
        List<ApUserSearch> apUserSearches = mongoTemplate.find(query1, ApUserSearch.class);
        return ResponseResult.okResult(apUserSearches);
    }

    /**
     * 删除搜索记录
     * @param dto
     * @return
     */
    @Override
    public ResponseResult del(HistorySearchDto dto) {
        //1.校验参数
        if(dto == null || StringUtils.isEmpty(dto.getId())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.根据传输的ID删除记录即可
        Query query1 = Query.query(Criteria.where("id").is(dto.getId()));
        mongoTemplate.remove(query1,ApUserSearch.class);
        return ResponseResult.okResult("操纵成功");
    }
}
