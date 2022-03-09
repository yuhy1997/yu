package com.heima.article.listener;


import com.alibaba.fastjson.JSON;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.HotArticleConstants;
import com.heima.model.mess.ArticleVisitStreamMess;
import com.heima.model.mess.UpdateArticleMess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 消费 聚合后的数据信息
 */
@Component
public class ApArticleInterHandlerListener {

    @Autowired
    private HotArticleService hotArticleService;

    @KafkaListener(topics =HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC)
    public void onMessage(String message){
        /**
         * 预先执行事件
         *
         */
        UpdateArticleMess updateArticleMess = JSON.parseObject(message,UpdateArticleMess.class);
        //1.把消息发送的 UpdateArticleMess 转换成  ArticleVisitStreamMess 对象
        ArticleVisitStreamMess mss = new ArticleVisitStreamMess();
        mss.setArticleId(updateArticleMess.getArticleId());
        switch (updateArticleMess.getType().name()){
            case "COLLECTION":
                mss.setCollect(updateArticleMess.getAdd());
                break;
            case "COMMENT":
                mss.setComment(updateArticleMess.getAdd());
                break;
            case "LIKES":
                mss.setLike(updateArticleMess.getAdd());
                break;
            case "VIEWS":
                mss.setView(updateArticleMess.getAdd());
                break;
        }
        //更新热点文章分值
        hotArticleService.updateScore(mss);
        System.out.println("消费数据:"+message);
    }
}
