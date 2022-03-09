package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.ChannelPageDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmLoginDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmUser;

public interface WmChannelService extends IService<WmChannel> {


    ResponseResult findAll();

    /**
     * 频道名称模糊分页查询
     */
    public ResponseResult list(ChannelPageDto dto);

}