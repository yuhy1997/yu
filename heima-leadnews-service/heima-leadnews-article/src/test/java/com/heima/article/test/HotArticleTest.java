package com.heima.article.test;

import com.heima.article.ArticleApplication;
import com.heima.article.service.HotArticleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class HotArticleTest {


    @Autowired
    private HotArticleService hotArticleService;

    @Test
    public void test(){
        hotArticleService.HotArticle();
        System.out.println("热点文章计算成功...");
    }

}
