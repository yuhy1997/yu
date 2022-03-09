package com.heima.wemedia.service.impl;

import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.common.ProtostuffUtil;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class WmNewsTaskServiceImpl implements WmNewsTaskService {

    @Autowired
    private IScheduleClient iScheduleClient;

    //封装任务对象，添加到任务队列中
    @Override
    public void addNewsToTask(Integer id, Date publishTime) {
        log.info("添加任务到延迟服务中----begin");
        //1.封装task对象
        Task task = new Task();
        //发布时间
        task.setExecuteTime(publishTime.getTime());
        task.setTaskType(1);
        task.setPriority(1);
        //wmnew对象序列化后的数据
        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        //存储是序列化后的值
        task.setParameters(ProtostuffUtil.serialize(wmNews));
        //2.使用feign 远程调用task表
        ResponseResult responseResult = iScheduleClient.addTask(task);

        log.info("添加任务到延迟服务中----end");
    }

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    /**
     * 消费任务
     * 定时执行
     */
    @Scheduled(fixedRate = 2000) //每秒执行一次
    @Override
    @SneakyThrows //忽略异常
    public void scanNewsByTask() {
        //1.远程拉去任务
        ResponseResult result = iScheduleClient.poll(1, 1);
        //2.请求成功，并且有返回值
        if(result.getCode().equals(200) && result.getData() != null){
            Task task = (Task) result.getData();
            byte[] parameters = task.getParameters();
            WmNews wmNews = ProtostuffUtil.deserialize(parameters, WmNews.class);
            //调用自动审核接口
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        }

    }
}
