package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper,ApUser> implements ApUserService {


    @Override
    public ResponseResult login(LoginDto loginDto) {
        //0.如果LoginDto没有用户名和密码，代表游客登录
        //业务逻辑
        //存在用户名和密码 代表用户登录
        if(StringUtils.isNotEmpty(loginDto.getPhone()) && StringUtils.isNotEmpty(loginDto.getPassword())){
            //1，用户输入了用户名和密码进行登录，校验成功后返回jwt(基于当前用户的id生成)
            //1.1 查询数据库是否存在此用户
            LambdaQueryWrapper<ApUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ApUser::getPhone,loginDto.getPhone());
            ApUser userDb = getOne(wrapper);
            if(userDb == null){//代表此用户不存在
                return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
            }
            //1.2 获得用户盐来进行密码校验  MD5+盐
            String salt = userDb.getSalt();
            //用户登录后的加密码
            String password = DigestUtils.md5DigestAsHex((loginDto.getPassword() + salt).getBytes());

            if(!password.equals(userDb.getPassword())){  //密码不一致
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);//返回密码错误
            }
            //1.3 密码校验成功，代表登录成功，生成JWT令牌返还给用户
            String token = AppJwtUtil.getToken(userDb.getId().longValue());

            //1.4 令牌返还给用户
            Map map = new HashMap();
            map.put("token",token);
            //返回的用户信息，记得清空密码和盐
            userDb.setSalt("");
            userDb.setPassword("");
            map.put("user",userDb);
            return ResponseResult.okResult(map);
        }else{
            //2，用户游客登录，生成jwt返回(基于默认值0生成)
            //2.1使用O生成一个游客的JWT
            String token = AppJwtUtil.getToken(0l);
            Map map = new HashMap();
            map.put("token",token);
            return ResponseResult.okResult(map);
        }

    }
}
