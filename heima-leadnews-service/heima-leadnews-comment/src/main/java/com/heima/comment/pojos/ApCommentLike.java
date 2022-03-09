package com.heima.comment.pojos;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document("ap_comment_like")
public class ApCommentLike implements Serializable {

    private String id;

    private Integer authorId;

    /*
    评论ID
     */
    private String commentId;


}
