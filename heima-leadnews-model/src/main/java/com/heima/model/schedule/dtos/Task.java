package com.heima.model.schedule.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class Task implements Serializable {

    /**
     * 任务id
     */
    private Long taskId;
    /**
     * 类型
     */
    private Integer taskType;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 执行id  (发布时间)
     */
    private long executeTime;

    /**
     * task参数  （文章数据）
     */
    private byte[] parameters;
    
}