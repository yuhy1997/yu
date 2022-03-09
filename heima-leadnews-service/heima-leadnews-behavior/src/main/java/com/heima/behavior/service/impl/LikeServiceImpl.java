package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.behavior.service.LikeService;
import com.heima.common.constants.HotArticleConstants;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.UpdateArticleMess;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.ApUserThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public static final String LIKES_ARTICLE = "likes_article_";

    public static final String LIKES_USER = "likes_user_";

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 给文章点赞或者取消点赞
     *      游客判断交给前端（id = 0）
     *      1.文章有哪些点赞
     *      2.查看用户点赞了那些文章
     *
     *      redis 可以做持久化 （存储到硬盘的文章中）
     * @param dto
     * @return
     */
    @Override
    public ResponseResult like(LikesBehaviorDto dto) {
        //1.校验参数
        if(dto == null||dto.getOperation() > 1 || dto.getOperation() < 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.判断是点赞呢还是取消点赞呢
        //2.1 获得登录的用户ID

        //3.发送消息给kafka 进行热点文章计算
        UpdateArticleMess mess = new UpdateArticleMess();
        //点赞的文章
        mess.setArticleId(dto.getArticleId());
        //操作
        mess.setType(UpdateArticleMess.UpdateArticleType.LIKES);



        ApUser user = ApUserThreadLocalUtil.getUser();
        if(user == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if(dto.getOperation() == 0){
            //3.点赞 redis 添加点赞数据
            redisTemplate.opsForZSet().add(LikeServiceImpl.LIKES_ARTICLE+dto.getArticleId(),user.getId().toString(),System.currentTimeMillis());
            //2.查看用户点赞了那些文章
            redisTemplate.opsForZSet().add(LikeServiceImpl.LIKES_USER+user.getId(),dto.getArticleId()+"",System.currentTimeMillis());

            mess.setAdd(1);

            System.out.println(mess);
            kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC, JSON.toJSONString(mess));

            return ResponseResult.okResult("点赞成功");
        }
        //4.redis 取消点赞数据
        redisTemplate.opsForZSet().remove(LikeServiceImpl.LIKES_ARTICLE+dto.getArticleId(),user.getId().toString());
        redisTemplate.opsForZSet().remove(LikeServiceImpl.LIKES_USER+user.getId(),dto.getArticleId()+"");
        //todo 调用article服务 进行文章的点赞或者取消点赞数更改
        mess.setAdd(-1);

        System.out.println(mess);
        kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC, JSON.toJSONString(mess));

        return ResponseResult.okResult("取消点赞");
    }
}
