package com.heima.schedule.test;


import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.ScheduleApplication;
import com.heima.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;


@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {

    @Autowired
    private TaskService taskService;

    @Test
    public void addTash(){
        Task task = new Task();
        task.setExecuteTime(System.currentTimeMillis()+220000);
        task.setPriority(1);
        task.setTaskType(1);
        task.setParameters(new byte[]{1,2,3,4});
        taskService.addTask(task);
    }

    @Test
    public void removeTask(){
        //redis 没有删除掉
        taskService.cancelTask(1494589305700012034l);
    }
    /**
     * 根据存储顺序取
     */
    @Test
    public void popTask(){ //62
        //lpush   rpop
        //取值的id没有修改
        Task poll = taskService.poll(1, 1);
        System.out.println(poll);
    }
}