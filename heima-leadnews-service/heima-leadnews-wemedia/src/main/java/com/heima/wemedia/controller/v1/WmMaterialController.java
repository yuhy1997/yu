package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 素材相关请求
 */
@RestController
@RequestMapping("/api/v1/material")
public class WmMaterialController {

    @Autowired
    private WmMaterialService wmMaterialService;

    /**
     * 图片上传接口
     * @param multipartFile
     * @return
     */
    @PostMapping("/upload_picture")
    public ResponseResult upload(MultipartFile multipartFile){
        return wmMaterialService.uploadPicture(multipartFile);
    }


    @PostMapping("/list")
    public ResponseResult list(@RequestBody WmMaterialDto dto){
        return wmMaterialService.list(dto);
    }


    /**
     * 图片的删除接口
     */
    @GetMapping("/del_picture/{id}")
    public ResponseResult delById(@PathVariable Integer id){
        return wmMaterialService.delById(id);
    }
}
