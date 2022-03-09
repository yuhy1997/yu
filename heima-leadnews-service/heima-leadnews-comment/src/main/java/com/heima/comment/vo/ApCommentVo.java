package com.heima.comment.vo;

import com.heima.comment.pojos.ApComment;
import lombok.Data;

@Data
public class ApCommentVo extends ApComment {

    /**
     * 0代表一点赞  null代表没点赞
     */
    private Integer operation;
}
