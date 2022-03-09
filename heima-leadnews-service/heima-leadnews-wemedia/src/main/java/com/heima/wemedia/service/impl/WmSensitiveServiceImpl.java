package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.admin.dtos.SensitivePageDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.service.WmSensitiveService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class WmSensitiveServiceImpl extends ServiceImpl<WmSensitiveMapper,WmSensitive> implements WmSensitiveService {

    /**
     * 查询列表
     * @param dto
     * @return
     */
    @Override
    public ResponseResult list(SensitivePageDto dto) {
        //1.检查参数
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.完美校验，校验一下分页参数是否有问题
        dto.checkParam();
        //3.条件分页查询
        IPage<WmSensitive> pageInfo = new Page<>(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<WmSensitive> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotEmpty(dto.getName())){
            wrapper.like(WmSensitive::getSensitives,dto.getName());
        }
        //添加排序规则
        wrapper.orderByDesc(WmSensitive::getCreatedTime);
        IPage<WmSensitive> page = page(pageInfo, wrapper);
        //4.返回结果对象
        PageResponseResult prr = new PageResponseResult((int)page.getCurrent(),(int)page.getSize(),(int)page.getTotal());
        prr.setData(page.getRecords());
        prr.setCode(200);
        prr.setErrorMessage("查询成功");
        return prr;
    }

    /**
     * 新增铭感词，但是不能重复
     * @param wmSensitive
     * @return
     */
    @Override
    public ResponseResult add(WmSensitive wmSensitive) {
        //1.判断参数
        if(null == wmSensitive || StringUtils.isEmpty(wmSensitive.getSensitives())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.校验数据是否重复
        LambdaQueryWrapper<WmSensitive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmSensitive::getSensitives,wmSensitive.getSensitives());
        WmSensitive sensitive = getOne(wrapper);
        if(sensitive !=null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST);
        }
        //3.保存到数据库 返回结果信息
        wmSensitive.setCreatedTime(new Date());
        save(wmSensitive);
        return ResponseResult.okResult("添加成功");
    }
}
