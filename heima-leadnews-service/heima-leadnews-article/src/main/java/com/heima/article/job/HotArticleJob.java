package com.heima.article.job;

import com.heima.article.service.HotArticleService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HotArticleJob {


    @Autowired
    private HotArticleService hotArticleService;

    /**
     * 1.引入 XXL-code 依赖
     * 2.复制配置类XXLCONFIG
     * 3.引入配置文件  执行器要注意
     * 4.任务类。HotArticleJob
     */
    @XxlJob("computeHotArticleJob")
    public void handler(){
        hotArticleService.HotArticle();
        System.err.println("热点文章计算结束。。。。。");
    }
}
