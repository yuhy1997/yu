package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.ApUserRealnameDto;
import com.heima.user.service.ApUserRealnameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class ApUserRealnameController {


    @Autowired
    private ApUserRealnameService apUserRealnameService;

    @PostMapping("/list")
    public ResponseResult list(@RequestBody ApUserRealnameDto dto){

        return apUserRealnameService.list(dto);
    }

    /**
     * 审核失败
     */
    @PostMapping("/authFail")
    public ResponseResult authFail(@RequestBody ApUserRealnameDto dto){
        return apUserRealnameService.authFail(dto);
    }


    /**
     * 审核失败
     */
    @PostMapping("/authPass")
    public ResponseResult authPass(@RequestBody ApUserRealnameDto dto){
        return apUserRealnameService.authPass(dto);
    }

}
