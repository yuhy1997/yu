package com.heima.model.comment.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentSaveDto implements Serializable {

    /**
     * 评论文章ID
     */
    private long articleId;

    /**
     * 评论内容
     */
    private String Content;
}
