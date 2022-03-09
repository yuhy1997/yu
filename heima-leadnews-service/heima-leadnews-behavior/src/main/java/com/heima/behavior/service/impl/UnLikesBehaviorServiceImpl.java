package com.heima.behavior.service.impl;

import com.heima.behavior.service.UnLikesBehaviorService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.ApUserThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UnLikesBehaviorServiceImpl implements UnLikesBehaviorService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 不喜欢
     * @param dto
     * @return
     */
    @Override
    public ResponseResult unLikes(UnLikesBehaviorDto dto) {
        //1.校验参数
        if(dto == null || dto.getType() > 1 || dto.getType() < 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.获得点击的用户
        ApUser user = ApUserThreadLocalUtil.getUser();
        if(user == null || user.getId() == 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if(dto.getType() == 0){
            //3.如果type = 0 代表添加不喜欢
            //zset  (分值) 排序  文章ID作为key    用户ID作为value  点击时间作为分值
            redisTemplate.opsForZSet().add(BehaviorConstants.UN_LIKES_BEHAVIOR + dto.getArticleId(),user.getId().toString(),System.currentTimeMillis());
        }else{
            //4. 如果type = 1 代表取消不喜欢
            redisTemplate.opsForZSet().remove(BehaviorConstants.UN_LIKES_BEHAVIOR + dto.getArticleId(),user.getId());
        }

        return ResponseResult.okResult("操作成功");
    }
}
