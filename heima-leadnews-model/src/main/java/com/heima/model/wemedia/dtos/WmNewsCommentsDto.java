package com.heima.model.wemedia.dtos;


import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class WmNewsCommentsDto extends PageRequestDto implements Serializable {


    /**
     * 开始时间
     */
    private Date beginDate;

    /**
     * 结束时间
     */
    private Date endDate;



    private Integer authorId;






}
