package com.heima.article.test;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.ArticleApplication;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.service.ApArticleService;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class FreemarkerTest {
    //1.注入一个配置对象
    @Autowired
    private Configuration configuration;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ApArticleService apArticleService;

    //此功能应该在文章发布时候调用接口
    @Test
    public void test() throws IOException, TemplateException {
        //1.获取文章内容
        LambdaQueryWrapper<ApArticleContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApArticleContent::getArticleId,1383827995813531650l);
        ApArticleContent apArticleContent = apArticleContentMapper.selectOne(wrapper);
        if(apArticleContent == null ){
            System.out.println("代码文章内容内空");
            return;
        }
        //2.文章内容通过freemarker生成html文件
        Template template = configuration.getTemplate("article.ftl");

        StringWriter out = new StringWriter(); //把流数据存储程字符串 （暂存）

        Map map = new HashMap();
        map.put("content",JSON.parseArray(apArticleContent.getContent()));
        template.process(map,out);

        InputStream is = new ByteArrayInputStream(out.toString().getBytes());

        //3.把html文件上传到minio中
        String path = fileStorageService.uploadHtmlFile(null, apArticleContent.getArticleId()+".html" + "", is);

        System.out.println("上传文件的访问路径为:"+path);
        //4.修改ap_article表，保存static_url字段
        ApArticle apArticle = new ApArticle();
        apArticle.setId(apArticleContent.getArticleId());
        apArticle.setStaticUrl(path);
        apArticleService.updateById(apArticle);
    }



}