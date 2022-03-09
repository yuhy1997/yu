package com.heima.model.comment.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CommentDto implements Serializable {

    /**
     * 文章的ID
     */
    private long articleId;

    /**
     * 最后一条评论的时间  获得更多评论请求的
     */
    private Date minDate;


    /**
     * index   1打开文章的第一个10条评论      2加载更多的评论数据
     */
    private Integer index;
}
