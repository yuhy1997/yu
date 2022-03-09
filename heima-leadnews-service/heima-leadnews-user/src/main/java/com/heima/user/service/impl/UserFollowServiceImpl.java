package com.heima.user.service.impl;

import com.heima.common.redis.CacheService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.user.service.UserFollowService;
import com.heima.utils.thread.ApUserThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserFollowServiceImpl implements UserFollowService {

    //用户关注前缀
    public static final String USER_FOLLOW= "user_follow_";


    //用户粉丝前缀
    public static final String USER_FANS = "user_fans";

    /**
     * redis工具类
     */
    @Autowired
    private CacheService cacheService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 关注或者取消关注
     *  所有的行为数据，都存储到redis中 Set  ZSet  hash
     * @param dto
     * @return
     */
    @Override
    public ResponseResult follow(UserRelationDto dto) {
        //1.校验参数
        if(dto == null||dto.getOperation()>1 || dto.getOperation() < 0 ){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.判断是关注还是取消关注
        Integer userId = ApUserThreadLocalUtil.getUser().getId();

        //3.用户不能关注自己
        if(userId  == dto.getAuthorId()){
            return ResponseResult.errorResult(5000,"不能关注自己");
        }

        if(dto.getOperation() == 0){
            //3.关注
            //3.1 给这个用户添加一个关注数据  userId    List
            cacheService.zAdd(UserFollowServiceImpl.USER_FOLLOW+userId,dto.getAuthorId()+"",System.currentTimeMillis());
            //redisTemplate.opsForZSet().add();
            //3.2 给这个作者添加一个粉丝数据  dto       List
            cacheService.zAdd(UserFollowServiceImpl.USER_FANS+dto.getAuthorId() ,userId+"",System.currentTimeMillis());
            return ResponseResult.okResult("关注成功");
        }else{
            //4.取消关注
            //4.1 删除用户的关注数据
            cacheService.zRemove(UserFollowServiceImpl.USER_FOLLOW+userId,dto.getAuthorId()+"");
            //redisTemplate.opsForZSet().remove();
            //4.2 删除作者的粉丝数据
            cacheService.zRemove(UserFollowServiceImpl.USER_FANS+dto.getAuthorId() ,userId+"");
            return ResponseResult.okResult("取消关注");
        }

    }
}
