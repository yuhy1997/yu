package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.SensitivePageDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmSensitive;

public interface WmSensitiveService extends IService<WmSensitive> {

    /**
     * 分页展示所有铭感词信息
     * @return  分页集合
     */
    public ResponseResult list(SensitivePageDto dto);

    ResponseResult add(WmSensitive wmSensitive);
}
