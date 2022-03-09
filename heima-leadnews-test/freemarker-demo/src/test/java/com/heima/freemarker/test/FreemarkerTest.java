package com.heima.freemarker.test;


import com.heima.freemarker.FreemarkerDemoApplication;
import com.heima.freemarker.entity.Student;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@SpringBootTest(classes = FreemarkerDemoApplication.class)
@RunWith(SpringRunner.class)
public class FreemarkerTest {
    //1.注入一个配置对象
    @Autowired
    private Configuration configuration;


    @Test
    public void test() throws IOException, TemplateException {
        //设置一个生成的页面的模板
        Template template = configuration.getTemplate("04-create.ftl");
        Map map = new HashMap();
        map.put("name","小张");
        map.put("msg","freemaker 不好用！！！！");
        //3.通过模板对象和数据来生成HTML页面
        template.process(map,new FileWriter("d:\\11.html"));
    }

}