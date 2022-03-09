package com.heima.model.behavior.dtos;

import lombok.Data;

/**
 * 用户阅读行为参数
 */
@Data
public class ReadBehaviorDto {


    /**
     * 阅读的文章
     */
    private long articleId;


    /**
     * 阅读次数  一般为1
     */
    private int count;
    /**
     * 加载时间
     */
    private long loadDuration;
    /**
     * 阅读量
     */
    private long percentage;
    /**
     * 文章阅读时间
     */
    private long readDuration;

}
