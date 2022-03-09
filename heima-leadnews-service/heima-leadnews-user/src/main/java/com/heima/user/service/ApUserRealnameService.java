package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.ApUserRealnameDto;
import com.heima.model.user.pojos.ApUserRealname;

public interface ApUserRealnameService extends IService<ApUserRealname> {
    /**
     * 查询所有需要实名认证得用户信息
     * @param dto
     * @return
     */
    ResponseResult list(ApUserRealnameDto dto);

    ResponseResult authFail(ApUserRealnameDto dto);

    ResponseResult authPass(ApUserRealnameDto dto);
}
