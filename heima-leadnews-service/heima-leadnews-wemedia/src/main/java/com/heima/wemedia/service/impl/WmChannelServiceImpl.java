package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.admin.dtos.ChannelPageDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper,WmChannel> implements WmChannelService {
    @Override
    public ResponseResult findAll() {

        List<WmChannel> list = list();

        return ResponseResult.okResult(list);
    }

    @Override
    public ResponseResult list(ChannelPageDto dto) {
        //1.校验分页参数
        dto.checkParam();
        //2.进行分页查询
        IPage<WmChannel> pageInfo = new Page<>(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<WmChannel> wrapper = new LambdaQueryWrapper<>();
        if(dto.getName() != null){//代表根据名查询
            wrapper.like(WmChannel::getName,dto.getName());
        }
        //根据时间排序
        wrapper.orderByDesc(WmChannel::getCreatedTime);
        IPage<WmChannel> page = page(pageInfo, wrapper);

        //3.数据封装
        PageResponseResult pageResponseResult = new PageResponseResult((int)page.getCurrent(),(int)page.getSize(),(int)page.getTotal());
        pageResponseResult.setData(page.getRecords());
        pageResponseResult.setCode(200);
        pageResponseResult.setErrorMessage("查询成功");
        return pageResponseResult;
    }
}
