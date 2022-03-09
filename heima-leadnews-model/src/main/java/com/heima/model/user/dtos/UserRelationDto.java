package com.heima.model.user.dtos;

import lombok.Data;

@Data
public class UserRelationDto {

    /**
     * 文章ID  （注意 精度丢失）
     */
    private long articleId;

    /**
     * 作者的ID
     */
    private int authorId;

    /**
     * 0关注  1取消
     */
    private short operation;

}
