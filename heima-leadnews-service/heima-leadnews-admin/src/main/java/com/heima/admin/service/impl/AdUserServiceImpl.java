package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdUserMapper;
import com.heima.admin.service.AdUserService;
import com.heima.model.admin.dtos.AdLoginDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.common.AppJwtUtil;
import org.jcodings.util.Hash;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdUserServiceImpl extends ServiceImpl<AdUserMapper,AdUser> implements AdUserService {


    @Override
    public ResponseResult login(AdLoginDto dto) {
        //1.校验参数
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);//参数错误
        }
        //2.根据用户名查询此用户
        LambdaQueryWrapper<AdUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdUser::getName,dto.getName());
        AdUser adUser = getOne(wrapper);
        if(adUser == null){//代表没有此用户
            return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
        }
        //3.获得数据库的盐加密铭文密码然后匹配数据库密码
        String password = DigestUtils.md5DigestAsHex((dto.getPassword()+adUser.getSalt()).getBytes());
        if(!password.equals(adUser.getPassword())){//加密后的密码不相等
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }
        //4.登录成功，生成JWT令牌
        String token = AppJwtUtil.getToken(adUser.getId().longValue());
        Map map = new HashMap();

        map.put("token",token);
        //密码和盐清空掉
        adUser.setPassword("");
        adUser.setSalt("");
        map.put("user",adUser);
        return ResponseResult.okResult(map);
    }
}
