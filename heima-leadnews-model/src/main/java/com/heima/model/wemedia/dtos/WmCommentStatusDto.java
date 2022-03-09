package com.heima.model.wemedia.dtos;

import lombok.Data;

/**
 * 打开或者关闭评论DTO
 */
@Data
public class WmCommentStatusDto {

    /**
     * 文章ID
     */
    private long articleId;

    /**
     * 0关闭评论 1开启评论
     */
    private Integer operation;

}
