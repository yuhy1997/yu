package com.heima.comment.pojos;


import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document("ap_comment")
public class ApComment implements Serializable {

    private String id;

    /**
     * 评论人ID
     */
    private Integer authorId;

    /**
     * 评论人名字
     */
    private String authorName;

    /**
     * 评论的文章ID
     */
    private long entryId;

    private int type;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 该评论的点赞数
     */
    private Integer likes;

    /**
     *该评论的回复数
     */
    private Integer reply;


    /**
     * 逻辑删除  0 正常   1 删除
     */
    private Integer flag;

    private Date createdTime;



}
