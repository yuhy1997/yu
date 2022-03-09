package com.heima.wemedia.controller.v1;

import com.heima.model.admin.dtos.ChannelPageDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 频道相关请求
 */
@RestController
@RequestMapping("/api/v1/channel")
public class WmchannelController {

    @Autowired
    private WmChannelService wmChannelService;

    @GetMapping("/channels")
    public ResponseResult findAll(){
        return wmChannelService.findAll();
    }



    @PostMapping("/list")
    public ResponseResult list(@RequestBody ChannelPageDto dto){
        return wmChannelService.list(dto);
    }

}
