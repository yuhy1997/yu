package com.heima;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 注意点：
 *  1.执行器的名字必须和xxl-job  appName 一致
 *
 *  2.@XxlJob("testJobHandler")  必须和xxl-job 任务中的JobHandler 一致
 *
 *  3.本机安装杀毒软件或者开启了防火墙
 *
 *  原理：
 *      发送HTTP请求给服务
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
