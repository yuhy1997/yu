package com.heima.wemedia.feign;

import com.alibaba.fastjson.JSON;
import com.heima.apis.wemedia.IWmUserClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WmUserClient implements IWmUserClient {

    @Autowired
    private WmUserService wmUserService;

    @PostMapping("/api/v1/wemedia/useradd")
    @Override
    public ResponseResult addWmUser(@RequestBody WmUser wmUser) {
        return wmUserService.addWmUser(wmUser);
    }


    @Autowired
    private WmChannelService wmChannelService;

    /**
     * 查询所有频道的信息Feign 接口
     * @return
     */
    @PostMapping("/api/v1/channel/findAll")
    @Override
    public ResponseResult getChannels() {

        List<WmChannel> list = wmChannelService.list();

        return ResponseResult.okResult(JSON.toJSONString(list));
    }
}
