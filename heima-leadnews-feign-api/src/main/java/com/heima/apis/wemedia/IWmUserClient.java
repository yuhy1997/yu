package com.heima.apis.wemedia;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 一个服务中 @FeignClient("leadnews-wemedia") 不能重复
 */
@FeignClient("leadnews-wemedia")
public interface IWmUserClient {


    @PostMapping("/api/v1/wemedia/useradd")
    public ResponseResult addWmUser(@RequestBody WmUser wmUser);


    /**
     * 查询所有频道的信息Feign 接口
     * @return
     */
    @PostMapping("/api/v1/channel/findAll")
    public ResponseResult getChannels();

}
