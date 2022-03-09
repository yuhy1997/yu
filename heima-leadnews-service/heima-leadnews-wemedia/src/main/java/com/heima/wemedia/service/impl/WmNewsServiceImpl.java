package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WmNewsMessageConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.*;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.model.wemedia.vos.WmNewsVo;
import com.heima.utils.common.AppJwtUtil;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmNewsTaskService;
import com.heima.wemedia.service.WmUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {


    @Override
    public ResponseResult findAll(WmNewsPageReqDto dto) {
        //1.参数校验
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.构建分页查询条件
        IPage<WmNews> page = new Page<>(dto.getPage(),dto.getSize());
        //3.构建查询条件
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>();
        //3.1 文章状态条件
        if(dto.getStatus() != null){
            wrapper.eq(WmNews::getStatus,dto.getStatus());
        }
        //3.2 文章频道查询
        if(dto.getChannelId() != null){
            wrapper.eq(WmNews::getChannelId,dto.getChannelId());
        }
        //3.3 文章关键字查询 （根据标题模糊查询）
        if(dto.getKeyword() != null){
            wrapper.like(WmNews::getTitle,dto.getKeyword());
        }
        //4.根据时间范围查询
        if(dto.getBeginPubDate() != null && dto.getEndPubDate() != null){
            wrapper.between(WmNews::getPublishTime,dto.getBeginPubDate(),dto.getEndPubDate());
        }
        IPage<WmNews> pageInfo = page(page, wrapper);

        //5.封装返回值对象
        PageResponseResult pageResponseResult = new PageResponseResult((int) pageInfo.getCurrent(), (int) pageInfo.getSize(), (int) pageInfo.getTotal());
        pageResponseResult.setData(pageInfo.getRecords());
        pageResponseResult.setCode(200);
        pageResponseResult.setErrorMessage("查询成功");
        return pageResponseResult;
    }

    @Autowired
    private WmMaterialMapper wmMaterialMapper;


    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    /**
     * 自媒体用户今天文章得添加
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submit(WmNewsDto dto) {
        //0 参数校验
        if(dto == null || dto.getContent() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //1.前端提交发布或保存为草稿
        //2.后台判断请求中是否包含了文章id
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto,wmNews);
        //把不能复制的数据转换程可以赋值的属性 《图片》
        //封面图片  list---> string
        if(dto.getImages() != null && dto.getImages().size() > 0){
            //[1dddfsd.jpg,sdlfjldk.jpg]-->   1dddfsd.jpg,sdlfjldk.jpg
            String imageStr = StringUtils.join(dto.getImages(), ",");
            wmNews.setImages(imageStr);
        }
        //如果当前封面类型为自动 -1  从内容中抽取图片  short 类型需要 == 来进行判断
        if(dto.getType() == -1){
            wmNews.setType(null);
        }
        //保存或者修改文章信息
        //1.补全数据
        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short)1);//默认上架
        //2.执行修改或者新增
        if(wmNews.getId() == null){
            //3.如果不包含id,则为新增
            //3.1 执行新增文章的操作
            save(wmNews);
        }else{
            //4.如果包含了id，则为修改请求
            //4.1 删除该文章与素材的所有关系
            //4.2 执行修改操作
            //关联关系删除   先删除后增原则
            LambdaQueryWrapper<WmNewsMaterial> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WmNewsMaterial::getNewsId,wmNews.getId());
            wmNewsMaterialMapper.delete(wrapper);
            updateById(wmNews);
        }

        //todo 无论是草稿还是文章都应该保存图片的关联关系
		//4.3 关联文章内容图片与素材的关系
        //4.3.1 获取内容中的所有图片
        List<Map> maps = JSON.parseArray(dto.getContent(),Map.class);
        //4.3.2 把内容中所有的图片封装到一个集合中
        List<String> images = new ArrayList<>();
        for (Map map : maps) {
            if(map.get("type").equals("image")){
                images.add((String) map.get("value"));
            }
        }
        //4.3.3 添加内容图片的关联关系 （查询素材的ID集合）
        int count = 0;
        if(images.size() > 0){
            LambdaQueryWrapper<WmMaterial> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(WmMaterial::getUrl,images);
            List<WmMaterial> wmMaterials = wmMaterialMapper.selectList(wrapper);
            for (WmMaterial wmMaterial : wmMaterials) {
                WmNewsMaterial wmNewsMaterial = new WmNewsMaterial();
                wmNewsMaterial.setNewsId(wmNews.getId());
                wmNewsMaterial.setMaterialId(wmMaterial.getId());
                wmNewsMaterial.setType((short) 0);
                wmNewsMaterial.setOrd((short) count++);
                wmNewsMaterialMapper.insert(wmNewsMaterial);
            }
        }
        //4.4 关联文章封面图片与素材的关系
        if(dto.getType() != 0 && dto.getImages().size() > 0){
            LambdaQueryWrapper<WmMaterial> wrapperFms = new LambdaQueryWrapper<>();
            wrapperFms.in(WmMaterial::getUrl,dto.getImages());
            List<WmMaterial> wmMaterialFms = wmMaterialMapper.selectList(wrapperFms);
            for (WmMaterial wmMaterial : wmMaterialFms) {
                WmNewsMaterial wmNewsMaterial = new WmNewsMaterial();
                wmNewsMaterial.setNewsId(wmNews.getId());
                wmNewsMaterial.setMaterialId(wmMaterial.getId());
                wmNewsMaterial.setType((short) 0);
                wmNewsMaterial.setOrd((short) count++);
                wmNewsMaterialMapper.insert(wmNewsMaterial);
            }
        }
        //4.5 如果用户选择的是自动
        if(wmNews.getType() == null){//代表选择的是自动
            //查看内容中有多少图片。然后设置不同的方式
            if(images.size() >= 3){ //代表应该是多图
                //取出前三找照片作为封面即可
                wmNews.setType((short)3);
                List<String> autoImg = images.stream().limit(3).collect(Collectors.toList());
                //设置封面图片数据   tu1,tu2,tu3
                String imageStr = StringUtils.join(autoImg, ",");
                wmNews.setImages(imageStr);
            }else if(images.size() >=1 && images.size()<3){ //代表是单图
                wmNews.setType((short)1);
                List<String> autoImg = images.stream().limit(1).collect(Collectors.toList());
                //设置封面图片数据
                wmNews.setImages(autoImg.get(0));
            }else{
                wmNews.setType((short)0);
            }
        }
        //4.6 更新数据库中文章信息
        updateById(wmNews);

        //todo  调用文章自动审核接口  autoScanWmNews(文章ID)  -- 优化 （多线程业务）
        //事件缩短呢？   用子线程去审核  同步执行
        //轻重缓急  马上发布，不需要马上发布
        //todo 添加一个任务到任务表中 （暂时存储一下）
        wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        //wmNewsTaskService.addNewsToTask(wmNews.getId(),wmNews.getPublishTime());

        return ResponseResult.okResult("提交成功");
    }

    @Autowired
    private WmNewsTaskService wmNewsTaskService;

    @Override
    public void test() {
        //1.提交文章到数据库  1S左右
        test1();
        //2.自动审核文章
        wmNewsAutoScanService.test2(); //子线程执行
    }
    //上下架功能
    @Override
    public ResponseResult up_down(WmNewsUpDownDto dto) {
        //1.校验参数
        if(dto.getId() == null || dto.getEnable() == null){ //
            //代表参数存在问题
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.根据id 查询出文章
        WmNews wmNews = getById(dto.getId());

        //3.文章不存在，直接结束方法，返回文章不存在
        if(null == wmNews){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //4.文章存在 判断文章得发布状态  0下架  1上架
        if(wmNews.getStatus() != 9 || dto.getEnable() < -1 || dto.getEnable() > 1 ){
            //代表参数存在问题
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //5.修改文章得发布状态 （enable ）
        wmNews.setEnable(dto.getEnable());
        updateById(wmNews);
        //todo 发送消息给kafka  通知文章artcle服务修改 文章配置信息
        //6.1 封装发送得数据 文章ID  上下架得状态
        HashMap map = new HashMap();
        if(wmNews.getArticleId() != null){
            map.put("artcleId",wmNews.getArticleId());
            map.put("enable",dto.getEnable());
        }
        //6.2 使用kafka 进行发送即可
        kafkaTemplate.send(WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC,JSON.toJSONString(map));
        return ResponseResult.okResult("操作成功");
    }

    /**
     * 分页查询自媒体文章
     * @param dto
     * @return
     */
    @Override
    public ResponseResult list_vo(NewsAuthDto dto) {
        //1.校验参数
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.分页参数校验
        dto.checkParam();
        //3.根据条件查询
        IPage<WmNews> pageInfo = new Page<>(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>() ;
        //4.根据标题查询
        if(StringUtils.isNotEmpty(dto.getTitle())){
            wrapper.like(WmNews::getTitle,dto.getTitle());
        }
        //5.根据状态查询
        if(dto.getStatus() != null){
            wrapper.eq(WmNews::getStatus,dto.getStatus());
        }
        //6.时间倒叙排序
        wrapper.orderByDesc(WmNews::getCreatedTime);
        IPage<WmNews> page = page(pageInfo, wrapper);

        List<WmNewsVo> wmNewsVos = new ArrayList<>();

        //7.封装数据
        List<WmNews> wmNewsList = page.getRecords();
        for (WmNews wmNews : wmNewsList) {
            WmNewsVo wmNewsVo = new WmNewsVo();
            //bean得复制
            BeanUtils.copyProperties(wmNews,wmNewsVo);
            //查询作者
            WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());

            wmNewsVo.setAuthorName(wmUser.getName());
            wmNewsVos.add(wmNewsVo);
        }
        PageResponseResult prr = new PageResponseResult((int)page.getCurrent(),(int)page.getSize(),(int)page.getTotal());
        prr.setData(wmNewsVos);
        prr.setCode(200);
        prr.setErrorMessage("操作成功");
        return prr;
    }

    @Autowired
    private WmUserMapper wmUserMapper;


    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;


    //此注解代表异步执行
    public  void test1(){
        //1.提交文章到数据库
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("提交到数据库....");
    }


    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

}