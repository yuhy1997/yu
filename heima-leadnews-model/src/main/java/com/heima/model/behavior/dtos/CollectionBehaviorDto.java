package com.heima.model.behavior.dtos;


import lombok.Data;

import java.util.Date;

@Data
public class CollectionBehaviorDto {

    /**
     * 文章ID
     */
    private long entryId;

    /**
     * 0收藏  1 取消收藏
     */
    private short operation;

    /**
     * 发布时间
     */
    private Date publishedTime;
    /**
     * 0 文章  1 动态
     */
    private short type;

}
