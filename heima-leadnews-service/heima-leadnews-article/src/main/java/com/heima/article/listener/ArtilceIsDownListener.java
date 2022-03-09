package com.heima.article.listener;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.common.constants.WmNewsMessageConstants;
import com.heima.model.article.pojos.ApArticleConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ArtilceIsDownListener {

    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    //监听自媒体文章上下架队列
    @KafkaListener(topics = WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void onMessage(String message){
        //1.检验参数
        if(StringUtils.isEmpty(message)){
            return;
        }
        //2.转换数据
        Map map = JSON.parseObject(message, Map.class);
        LambdaUpdateWrapper<ApArticleConfig> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ApArticleConfig::getArticleId,map.get("artcleId"));
        //自媒体文章中，enable  0 (下架)  1（上架）
        short enable1 = Short.parseShort(map.get("enable") + "");
        wrapper.set(ApArticleConfig::getIsDown,enable1==(short)0?true:false);
        apArticleConfigMapper.update(null,wrapper);
        //3.打印成功即可
        System.out.println("文章上下架成功");
    }
}
