package com.heima.model.user.dtos;

import lombok.Data;

/**
 * 用户登录DTO
 */
@Data
public class LoginDto {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;
}