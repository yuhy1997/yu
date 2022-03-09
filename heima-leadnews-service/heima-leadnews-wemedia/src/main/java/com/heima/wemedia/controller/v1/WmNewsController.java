package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.NewsAuthDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.dtos.WmNewsUpDownDto;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文章相关请求
 */
@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {

    @Autowired
    private WmNewsService wmNewsService;

    /**
     * 查询文章列表
     * @param dto
     * @return
     */
    @PostMapping("/list")
    public ResponseResult findAll(@RequestBody WmNewsPageReqDto dto){
        return wmNewsService.findAll(dto);
    }


    /**
     * 提交文章 修改，新增，保存草稿
     */
    @PostMapping("/submit")
    public ResponseResult submit(@RequestBody WmNewsDto dto){
        return wmNewsService.submit(dto);
    }

    /**
     * 模拟测试多线程得异步提交
     */
    @GetMapping("/test")
    public String test(){
        wmNewsService.test();
        return "success";
    }

    /**
     * 文章得上架和下架功能
     */
    @PostMapping("/down_or_up")
    public ResponseResult down_or_up(@RequestBody WmNewsUpDownDto dto){

        return wmNewsService.up_down(dto);
    }


    /**
     * 根据条件分页查询自媒体文章数据
     */
    @PostMapping("/list_vo")
    public ResponseResult list_vo(NewsAuthDto dto){

        return wmNewsService.list_vo(dto);
    }

}
