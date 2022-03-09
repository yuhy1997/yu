package com.heima.search.listener;


import com.alibaba.fastjson.JSON;
import com.heima.model.search.vos.SearchArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class SyncArticleListener {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @KafkaListener(topics = "searchInsert")
    public void OnMessage(String message){
        //1.判断参数是否存在
        if(StringUtils.isEmpty(message)){
            return;
        }
        //2.转换成一个对象
        SearchArticleVo searchArticleVo = JSON.parseObject(message, SearchArticleVo.class);

        //3.调用方法保存到es 中
        IndexRequest indexRequest = new IndexRequest("app_info_article");
        indexRequest.id(searchArticleVo.getId().toString());
        indexRequest.source(message, XContentType.JSON);
        try {
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("sync es error={}",e);
        }

    }

}
