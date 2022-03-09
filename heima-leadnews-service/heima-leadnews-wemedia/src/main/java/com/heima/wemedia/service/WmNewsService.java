package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.*;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmUser;
import org.springframework.web.bind.annotation.RequestBody;

public interface WmNewsService extends IService<WmNews> {


    /**
     * 根据条件查询作者的文章信息
     */
    ResponseResult findAll(WmNewsPageReqDto dto);

    /**
     * 提交文章 修改，新增，保存草稿
     */
    public ResponseResult submit(WmNewsDto dto);

    void test();

    /**
     * 文章得上架和下架功能
     */
    public ResponseResult up_down(WmNewsUpDownDto dto);

    ResponseResult list_vo(NewsAuthDto dto);
}