package com.heima.model.wemedia.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class WmNewsUpDownDto implements Serializable {

    private Integer id;
    /**
     * 是否上架  0 下架  1 上架
     */
    private Short enable;
}
