package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.behavior.pojo.ReadBehavior;
import com.heima.behavior.service.ReadBehaviorService;
import com.heima.common.constants.HotArticleConstants;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.UpdateArticleMess;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.ApUserThreadLocalUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReadBehaviorServiceImpl implements ReadBehaviorService {

    public static final String READ_ARTICLE_BEHAVIOR = "read_article_behavior_";

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * Hash   大key 小key value     文章ID   用户ID   行为对象
     *
     * 用户的阅读行为记录
     * @param dto
     * @return
     */
    @Override
    public ResponseResult read(ReadBehaviorDto dto) {
        //1.校验参数
        if(dto == null || dto.getCount() != 1){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //1.1 获得用户的ID
        ApUser user = ApUserThreadLocalUtil.getUser();
        if(user == null || user.getId() == 0){
            //如果用户不粗在，或者用户是游客那请先登录
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        //2.查询用户是否阅读过这篇文章
        Object o = redisTemplate.opsForHash().get(ReadBehaviorServiceImpl.READ_ARTICLE_BEHAVIOR+dto.getArticleId(), user.getId().toString());

        if(o == null){ //代表用户没有看过这篇文章
            //4.没阅读过 -- 新增用户阅读行为
            redisTemplate.opsForHash().put(ReadBehaviorServiceImpl.READ_ARTICLE_BEHAVIOR+dto.getArticleId(),user.getId().toString(), JSON.toJSONString(dto));
        }else{
            //3.阅读过 -- 次数+1 时长累加
            //3.1 把JSON 转换成DTO对象
            ReadBehaviorDto readBehaviorDto = JSON.parseObject((String) o, ReadBehaviorDto.class);
            //3.2 更新次数 更新时长
            readBehaviorDto.setCount(readBehaviorDto.getCount()+1);
            readBehaviorDto.setReadDuration(readBehaviorDto.getReadDuration() + dto.getReadDuration());
            //3.3.更新到redis 中
            redisTemplate.opsForHash().put(ReadBehaviorServiceImpl.READ_ARTICLE_BEHAVIOR+dto.getArticleId(),user.getId().toString(), JSON.toJSONString(readBehaviorDto));
        }

        //3.发送消息给kafka 进行热点文章计算
        UpdateArticleMess mess = new UpdateArticleMess();
        //点赞的文章
        mess.setArticleId(dto.getArticleId());
        //操作
        mess.setType(UpdateArticleMess.UpdateArticleType.VIEWS);

        mess.setAdd(1);

        kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC, JSON.toJSONString(mess));

        return ResponseResult.okResult("操作成功");
    }

    @Autowired
    private MongoTemplate mongoTemplate;


    public ResponseResult readToMongo(ReadBehaviorDto dto){
        //1.校验参数
        if(dto == null || dto.getCount() != 1){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //1.1 获得用户的ID
        ApUser user = ApUserThreadLocalUtil.getUser();
        if(user == null || user.getId() == 0){
            //如果用户不粗在，或者用户是游客那请先登录
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        //2.查询用户是否阅读过这篇文章
        Query query = new Query(Criteria.where("articleId").is(dto.getArticleId()).and("userId").is(user.getId()));
        ReadBehavior readBehavior = mongoTemplate.findOne(query, ReadBehavior.class);
        if(readBehavior == null){
            readBehavior = new ReadBehavior();
            //4.没阅读过 -- 新增用户阅读行为
            BeanUtils.copyProperties(dto,readBehavior);
            //不全用户ID
            readBehavior.setUserId(user.getId());
            mongoTemplate.save(readBehavior);
        }else{
            //3.阅读过 -- 次数+1 时长累加
            readBehavior.setCount(readBehavior.getCount() + dto.getCount());
            readBehavior.setReadDuration(readBehavior.getReadDuration()+ dto.getReadDuration());
            mongoTemplate.save(readBehavior);
        }
        return ResponseResult.okResult("操作成功");
    }
}
