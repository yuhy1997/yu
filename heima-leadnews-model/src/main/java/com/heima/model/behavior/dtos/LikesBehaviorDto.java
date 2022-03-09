package com.heima.model.behavior.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class LikesBehaviorDto implements Serializable {

    /**
     * 点赞文章ID
     */
    private long articleId;

    /**
     * 0 点赞   1 取消点赞
     */
    private short operation;

    /**
     * 0文章  1动态   2评论
     */
    private short type;
}
