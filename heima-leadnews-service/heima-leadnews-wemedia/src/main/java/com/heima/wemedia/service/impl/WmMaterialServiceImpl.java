package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper,WmMaterial> implements WmMaterialService {

    @Autowired
    private FileStorageService fileStorageService;



    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        //1.校验参数是否合法
        if(multipartFile == null){ //参数不合法
            //返回无效参数
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.把图片上传到minio
        //2.1 生成一个随机的文件名字
        String fileName = UUID.randomUUID().toString().replace("-", "");
        //2.2 获得文件的后缀 JPG PNG JPGE
        String profixName = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
        try {
            String url = fileStorageService.uploadImgFile(null, fileName + profixName, multipartFile.getInputStream());
            //3.把上传好的图片地址存储到数据库表中
            WmMaterial wmMaterial = new WmMaterial();
            wmMaterial.setCreatedTime(new Date());
            wmMaterial.setIsCollection((short)0);
            wmMaterial.setType((short) 0);
            wmMaterial.setUrl(url);
            wmMaterial.setUserId(WmThreadLocalUtil.getUser().getId());
            save(wmMaterial);
            //4.响应结果
            return ResponseResult.okResult(wmMaterial);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }

    }

    @Override
    public ResponseResult list(WmMaterialDto dto) {
        //1.校验参数
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.查询数据
        IPage<WmMaterial> page = new Page<>(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<WmMaterial> wrapper = new LambdaQueryWrapper<>();
        if(dto.getIsCollection() == 1){
            wrapper.eq(WmMaterial::getIsCollection,dto.getIsCollection());
        }
        //只能查询自己的素材
        wrapper.eq(WmMaterial::getUserId,WmThreadLocalUtil.getUser().getId());
        IPage<WmMaterial> page1 = page(page, wrapper);

        //3.封装返回对象
        PageResponseResult pageResponseResult = new PageResponseResult((int) page1.getCurrent(), (int) page1.getSize(), (int) page1.getTotal());
        pageResponseResult.setData(page1.getRecords());
        pageResponseResult.setCode(200);
        pageResponseResult.setErrorMessage("成功");
        return pageResponseResult;
    }



    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Override
    public ResponseResult delById(Integer id) {
        //业务思路：
    	//1.删除图片的前提，不能被文章所使用
        // 查询wm_news_material 中间表，是否存在数据
        LambdaQueryWrapper<WmNewsMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmNewsMaterial::getMaterialId,id);
        List<WmNewsMaterial> wmNewsMaterials = wmNewsMaterialMapper.selectList(wrapper);

        if(wmNewsMaterials.size() > 0){//代表不能删除此素材
            return  ResponseResult.errorResult(AppHttpCodeEnum.PARAM_IMAGE_USE);
        }
        //2.删除数据库素材数据
        WmMaterial wmMaterial = getById(id);
        removeById(id);

        //3.在minio 把图片真实的删除掉
        fileStorageService.delete(wmMaterial.getUrl());

        return ResponseResult.okResult("删除成功");
    }
}
