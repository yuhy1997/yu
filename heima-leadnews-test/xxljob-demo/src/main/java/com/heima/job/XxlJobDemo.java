package com.heima.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class XxlJobDemo {

    /**
     *  http://192.168.21.170:9991/testJobHandler
     * 定时执行的方法
     *  @XxlJob("demo1JobHandler") 名字必须和配置的任务的JOBhandler 一模一样
     */
    @XxlJob("testJobHandler")
    public void jobTest(){

        System.out.println("我执行了.."+new Date());

    }
}
