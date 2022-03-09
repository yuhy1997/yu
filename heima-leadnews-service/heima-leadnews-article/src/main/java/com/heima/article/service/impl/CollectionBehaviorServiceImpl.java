package com.heima.article.service.impl;

import com.heima.article.service.CollectionBehaviorService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.model.behavior.dtos.CollectionBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.ApUserThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CollectionBehaviorServiceImpl implements CollectionBehaviorService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public ResponseResult collection(CollectionBehaviorDto dto) {
        //1.校验参数
        if(dto == null || dto.getType() > 1 || dto.getType() < 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.获得点击的用户
        ApUser user = ApUserThreadLocalUtil.getUser();
        if(user == null || user.getId() == 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //2.判断是收藏还是取消收藏
        if(dto.getOperation() == 0){//收藏
            //3.收藏 添加收藏数据到redis
            //大key (文章ID)  小key 用户ID     value()
            redisTemplate.opsForZSet().add(BehaviorConstants.COLLECTION_ARTICLE_BEHAVIOR + dto.getEntryId(),user.getId().toString(),System.currentTimeMillis());
            redisTemplate.opsForZSet().add(BehaviorConstants.COLLECTION_USER_BEHAVIOR + user.getId(),dto.getEntryId()+"",System.currentTimeMillis());

        }else{
            //4.取消收藏 删除redis数据
            redisTemplate.opsForZSet().remove(BehaviorConstants.COLLECTION_ARTICLE_BEHAVIOR + dto.getEntryId(),user.getId().toString());
            redisTemplate.opsForZSet().remove(BehaviorConstants.COLLECTION_USER_BEHAVIOR + user.getId(),dto.getEntryId()+"");
        }

        return ResponseResult.okResult("操作成功");
    }
}
