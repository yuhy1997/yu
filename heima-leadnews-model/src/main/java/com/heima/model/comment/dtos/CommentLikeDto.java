package com.heima.model.comment.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentLikeDto implements Serializable {

    /**
     * 评论ID
     */
    private String commentId;

    /**
     * 0点赞   1 取消点赞
     */
    private short operation;

}
