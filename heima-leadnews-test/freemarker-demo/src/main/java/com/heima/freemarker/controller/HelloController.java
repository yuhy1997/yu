package com.heima.freemarker.controller;

import com.heima.freemarker.entity.Student;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;


@Controller
public class HelloController {

    //model = request
    @GetMapping("/basic")
    public String hello(Model model){
        //------------------------------------
        Student stu1 = new Student();
        stu1.setName("小强");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        //小红对象模型数据
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);

        //将两个对象模型数据存放到List集合中
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);

        model.addAttribute("stus",stus);
        return "01-basic";
    }



    @GetMapping("/basic03")
    public String hello3(Model model){
        //------------------------------------
        Student stu1 = new Student();
        stu1.setName("小强");
        stu1.setAge(20);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        model.addAttribute("stu",stu1);
        return "03-if";
    }



    @GetMapping("/list")
    public String list(Model model){
        //------------------------------------
        Student stu1 = new Student();
        stu1.setName("小强");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        //小红对象模型数据
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);

        //将两个对象模型数据存放到List集合中
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);

        //向model中存放List集合数据
        model.addAttribute("stus",stus);

        //map数据
        Map<String,Student> stuMap = new HashMap<>();
        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);

        model.addAttribute("stuMap",stuMap);
        //日期
        model.addAttribute("today",new Date());

        //长数值
        model.addAttribute("point",38473897438743L);

        return "02-list";
    }

}
