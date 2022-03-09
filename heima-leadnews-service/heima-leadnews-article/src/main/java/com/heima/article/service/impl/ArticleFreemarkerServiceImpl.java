package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {
    //1.注入一个配置对象freemarker
    @Autowired
    private Configuration configuration;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Override
    public void buildArticleToMinIO(ApArticle apArticle, String content) {
        //1.获取文章内容 不需要完成

        //2.文章内容通过freemarker生成html文件
        try {
            Template template = configuration.getTemplate("article.ftl");

            StringWriter out = new StringWriter();
            //设置参数，生成页面
            Map map = new HashMap();
            map.put("content",JSON.parseArray(content));
            map.put("apArticle",apArticle);
            template.process(map,out);
            //把生成的文件转换为输入流
            InputStream is = new ByteArrayInputStream(out.toString().getBytes());
            //3.把html文件上传到minio中
            String path = fileStorageService.uploadHtmlFile(null, apArticle.getId()+".html" + "", is);
            //4.保存文章上传后的静态页面的路径
            apArticle.setStaticUrl(path);
            apArticleMapper.updateById(apArticle);
            System.out.println("生成静态页面成功");
        } catch (Exception e) {
            System.out.println("生成静态页面失败");
            e.printStackTrace();
        }
    }
}
