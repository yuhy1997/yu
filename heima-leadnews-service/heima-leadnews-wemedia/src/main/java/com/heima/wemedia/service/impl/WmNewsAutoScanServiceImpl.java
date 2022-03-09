package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.injector.methods.UpdateById;
import com.heima.apis.article.IArticleClient;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {


    //spring 在增强某个方法必须依赖于接口  AOP
    @Override
    @Async
    public void test2(){
        //2.自动审核文章
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("自动审核文章....");
    }

    @Autowired
    private WmNewsMapper wmNewsMapper;
    //图片审核接口
    @Autowired
    private GreenImageScan greenImageScan;

    //文本审核接口
    @Autowired
    private GreenTextScan greenTextScan;

    //minio服务器接口
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private IArticleClient iArticleClient;

    @Autowired
    private WmChannelMapper wmChannelMapper;

    @Autowired
    private WmUserMapper wmUserMapper;

    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;

    /**
     * 自动审核接口
     * @param id 自媒体文章ID  run  = autoScanWmNews
     *           子线程
     */
    @Async
    @Override
    public void autoScanWmNews(Integer id) {

        //1.校验文章是否存在
        WmNews wmNews = wmNewsMapper.selectById(id);
        if(wmNews == null){
            //文章不存在
            throw new RuntimeException("审核文章不存在.....");
        }
        //1.1 抽取文本和图片 wmNews
        //todo 图片中得文字需要从这里抽取，然后集成文本数据中（稍后补充）
        Map map = handlerTextAndImg(wmNews);

        //todo 2.有些铭感词是自己公司独有的。迎合时代的独有的铭感词  DFA 进行自铭感词匹配
        boolean textSensitive = checkSensitive((String) map.get("text"), wmNews);
        if(!textSensitive){ //代表返回值为false 那么就出现了铭感词
            return;
        }

        //todo 2.进行文本审核   成功 失败
        boolean textFlag = checkedText((String) map.get("text"), wmNews);
        if(!textFlag){ //如果返回值为false 那么代表文本审核不通过，那么就没必要往后执行了
            return;
        }
        //todo 1.图片你面带了违法文字  识别不了的
        //3.进行图片审核
       /* boolean imgFlag = checkedImgs((List<String>) map.get("images"), wmNews);
        if(!imgFlag){
            return;
        }*/

        //4.保存APP端相关文章数据
        //4.1 封装ApArticleDto 对象
        //1.封装一个ArticleDto
        ArticleDto articleDto = new ArticleDto();
        //2.属性拷贝
        BeanUtils.copyProperties(wmNews , articleDto);
        //3.设置文章布局
        articleDto.setLayout(wmNews.getType());
        //4.补全频道名称
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        articleDto.setChannelName(wmChannel.getName());
        //5.补充作者信息
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        articleDto.setAuthorId(wmUser.getId().longValue());
        articleDto.setAuthorName(wmUser.getName());
        //6.如果修改文章,设置文章ID  如果存在这个ArticleId
        // 那么代表这个文章是以前已经发布过的文章进行修改的
        if(wmNews.getArticleId() != null){
            articleDto.setId(wmNews.getId().longValue());
        }

        //把wmnews数据保存到article 表中
        ResponseResult responseResult = iArticleClient.saveArticle(articleDto);
        //更新自媒体文章的文章ID

        wmNews.setArticleId((Long) responseResult.getData());
        //更新自媒体文章
        wmNews.setStatus((short)9);
        wmNews.setReason("审核成功");
        wmNewsMapper.updateById(wmNews);

        //todo  文章在发布审核通过结束后，生成文章详情得静态页面

    }

    /**
     * 自定义铭感词匹配
     * @param text  需要进行校验得文本信息
     * @param wmNews 文章对象
     * @return 放行返回true 不放行返回false
     */
    private boolean checkSensitive(String textSensitive,WmNews wmNews){
        //1.查询铭感词表获得所有得铭感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(null);
        //1.1 获得集合中所有对象得名字
        List<String> collect = wmSensitives.stream()
                .map(c -> c.getSensitives()).collect(Collectors.toList());
        //2.调用工具类，创建DFA表
        SensitiveWordUtil.initMap(collect);
        //3.把内容交给DFA进行匹配
        Map<String, Integer> text = SensitiveWordUtil.matchWords(textSensitive);
        if(text.size() > 0){//代表有铭感词
            System.out.println("有铭感词");
            //修改文章表的状态信息
            wmNews.setStatus((short) 2);
            //设置拒绝历有
            wmNews.setReason("存在违规文本信息");
            //跟新数据库
            wmNewsMapper.updateById(wmNews);
            return false;
        }
        return true;
    }


    @Autowired
    private Tess4jClient tess4jClient;

    /**
     * 图片审核接口
     */
    private boolean checkedImgs(List<String> images,WmNews wmNews){
        //0. 封面图片和内容图片可能一样。 （去重一下）
        images = images.stream().distinct().collect(Collectors.toList());
        //1.把minio图片下载到本地
        List<byte[]> urls = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        if(images == null || images.size() <=0){

        }
        for (String image : images) {
            //一张一张下载，然后封装到一个新的字节数组集合中
            byte[] bytes = fileStorageService.downLoadFile(image);

            //从图片中，抽取所有得文字进行审核
            //从byte[]转换为butteredImage
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            BufferedImage imageFile = null;
            try {
                imageFile = ImageIO.read(in);
                //得到图片得文字，累加所有得图片得文字，一次性审核
                String s = tess4jClient.doOCR(imageFile);
                sb.append(s);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TesseractException e) {
                e.printStackTrace();
            }
            urls.add(bytes);
        }
        //进行铭感词匹配即可
        boolean b = checkSensitive(sb.toString(), wmNews);
        if(!b){
            return false;
        }
        //2.提交给阿里云审核
        try {
            //pass  / review / block
            Map map = greenImageScan.imageScan(urls);
            String suggestion = (String) map.get("suggestion");
            if(suggestion.equals("block")){//文本违规
                //修改文章表的状态信息
                wmNews.setStatus((short) 2);
                //设置拒绝历有
                wmNews.setReason("存在违规图片信息");
                //跟新数据库
                wmNewsMapper.updateById(wmNews);
                return false;
            }else if(suggestion.equals("review")){//代表没法确定 切换到人工审核
                wmNews.setStatus((short) 3);
                wmNews.setReason("等待人工审核");
                //跟新数据库
                wmNewsMapper.updateById(wmNews);
                return false;
            }else{//代表是pass 可以直接放行
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 文本审核接口
     * @return
     */
    private boolean checkedText(String content,WmNews wmNews){
        //校验参数
        if(content == null){
            return false;
        }
        try {
            //suggestion = block
            Map map = greenTextScan.greeTextScan(content);
            String suggestion = (String) map.get("suggestion");
            if(suggestion.equals("block")){//文本违规
                //修改文章表的状态信息
                wmNews.setStatus((short) 2);
                //设置拒绝历有
                wmNews.setReason("存在违规文本信息");
                //跟新数据库
                wmNewsMapper.updateById(wmNews);
                return false;
            }else if(suggestion.equals("review")){//代表没法确定 切换到人工审核
                wmNews.setStatus((short) 3);
                wmNews.setReason("等待人工审核");
                //跟新数据库
                wmNewsMapper.updateById(wmNews);
                return false;
            }else{//代表是pass 可以直接放行
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    //抽取文章中的所有图片和文本信息
    private Map handlerTextAndImg(WmNews wmNews){
        String content = wmNews.getContent();
        List<Map> maps = JSON.parseArray(content, Map.class);
        //创建一个集合来存储所有的图片
        //存储文本信息
        StringBuilder stringBuilder = new StringBuilder();
        //存储图片信息
        List<String> images = new ArrayList<>();

        for (Map map : maps) {
            if(map.get("type").equals("text")){ //代表是文本信息
                stringBuilder.append(map.get("value"));
            }
            if(map.get("type").equals("image")){
                images.add((String) map.get("value"));
            }
        }
        //收集除开内容外的图片以及文本
        stringBuilder.append(wmNews.getTitle());
        String[] split = wmNews.getImages().split(",");
        images.addAll(Arrays.asList(split));
        Map map = new HashMap();
        map.put("text",stringBuilder.toString());
        map.put("images",images);
        return map;
    }
}
