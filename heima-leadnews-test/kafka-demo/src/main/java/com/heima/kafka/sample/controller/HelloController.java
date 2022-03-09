package com.heima.kafka.sample.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * kafka发送消息得请求
     */
    @GetMapping("/hello")
    public String sendMsg(String articleId , String score){
        kafkaTemplate.send("itcast-topic-input",articleId+" "+score);
        return "success";
    }

}
