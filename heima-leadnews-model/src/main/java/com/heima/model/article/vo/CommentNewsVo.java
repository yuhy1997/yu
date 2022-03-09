package com.heima.model.article.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CommentNewsVo implements Serializable {

    /**
     * 文章ID
     */
    private long id;

    /**
     * 文章的标题
     */
    private String title;

    /**
     * 评论数量
     */
    private Integer comments;

    /**
     * 是否开启评论  小boolean  会忽略is
     */
    private Boolean isComment;

    private Date createdTime;

}
