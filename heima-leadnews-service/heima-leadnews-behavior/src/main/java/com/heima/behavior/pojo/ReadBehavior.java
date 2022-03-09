package com.heima.behavior.pojo;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("ap_read_behavior")
public class ReadBehavior {

    private String id;

    /**
     * 用户的ID
     */
    private Integer userId;
    /**
     * 阅读的文章
     */
    private long articleId;
    /**
     * 阅读次数  一般为1
     */
    private int count;
    /**
     * 加载时间
     */
    private long loadDuration;
    /**
     * 阅读量
     */
    private long percentage;
    /**
     * 文章阅读时间
     */
    private long readDuration;

}
