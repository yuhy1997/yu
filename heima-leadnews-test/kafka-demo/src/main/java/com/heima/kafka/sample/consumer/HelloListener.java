package com.heima.kafka.sample.consumer;

import com.heima.kafka.sample.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
//低耦合  高内聚
@Component
public class HelloListener {

    @Autowired
    private HelloService helloService;

    /**
     *  不需要返回值得情况下，那么使用MQ队列
     *  方法需要返回值做后续判断，那么使用Feign
     *
     *
     *  id  11111    score  300    一段时间聚合内的数据
     *  id  11112    score   10000
     *
     * @param
     */
    @KafkaListener(topics = "itcast-topic-out")
    public void listennerMsg(String key,String value){
        /*System.out.println("我接收到了监听消息message:"+key+"--"+value);
        helloService.hello();*/
        System.out.println("执行完成....");
    }
}
