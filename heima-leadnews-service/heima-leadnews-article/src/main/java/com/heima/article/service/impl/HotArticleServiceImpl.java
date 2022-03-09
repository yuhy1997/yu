package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.wemedia.IWmUserClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.vo.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.mess.ArticleVisitStreamMess;
import com.heima.model.wemedia.pojos.WmChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.nntp.Article;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class HotArticleServiceImpl implements HotArticleService {


    @Autowired
    private ApArticleMapper apArticleMapper;

    @Override
    public void HotArticle() {
        //1.查询前5天的文章
        //1.1 获取当前时间前五天的时间
        Date dayParam = DateTime.now().minusDays(5).toDate();

        List<ApArticle> apArticles = apArticleMapper.findArticleListByLast5days(dayParam);

        //2.计算分值
        //2.1 准备工作 构建一个ArticleVo   分值常量
        List<HotArticleVo> articleVos = new ArrayList<>();
        for (ApArticle apArticle : apArticles) {
            HotArticleVo hav = new HotArticleVo();
            BeanUtils.copyProperties(apArticle,hav);
            //2.2 计算每篇文章的分支
            hav.setScore(computeArticleScore(hav));
            //存储到集合中
            articleVos.add(hav);
        }

        //3.为每个频道缓存30条分值较高的文章
        //3.1 根据分类把每个频道的文章分别进行缓存
        //3.1.1 获取到所有的频道
        ResponseResult responseResult = iWmChannelClient.getChannels();
        //3.1.2 校验远程请求数据是否存在
        if(responseResult.getCode() == 200 && responseResult.getData() != null){
            //3.1.3 如果频道查询成功，分拣文章
            String str = (String) responseResult.getData();
            List<WmChannel> channels = JSON.parseArray(str, WmChannel.class);
            //循环查询出每个频道的文章信息
            for (WmChannel channel : channels) {
                //通过频道的ID 和 文章中的频道ID 做匹配，如果匹配成功代表是该频道的文章
                /*Integer id = channel.getId();
                List<HotArticleVo> newChannelArticle = new ArrayList<>();
                for (HotArticleVo articleVo : articleVos) {
                    if(articleVo.getChannelId() == id){
                        //存入到一个新的集合中
                        newChannelArticle.add(articleVo);
                    }
                     //循环结束newChannelArticle 集合就是符合该频道ID的文章
                }*/
                //集合就是符合该频道ID的文章  collect
                List<HotArticleVo> collect = articleVos.stream()
                        .filter(c -> c.getChannelId() == channel.getId()).collect(Collectors.toList());

                //存入到redis 中  排序  存储到redis (key(频道ID)    value(文章的集合))
                sortAndToRedis(collect,ArticleConstants.HOT_ARTICLE_PAGE + channel.getId());
            }
        }
        //3.2 推荐列表的前30文章页需要进行存储操作
        sortAndToRedis(articleVos,ArticleConstants.HOT_ARTICLE_PAGE + ArticleConstants.DEFAULT_TAG);


    }

    @Override
    public void updateScore(ArticleVisitStreamMess avsm) {

        //1.更新数据库的点赞，阅读..数量
        ApArticle apArticle = updateArticleDb(avsm);

        //2.计算文章分值
        HotArticleVo hotArticleVo = new HotArticleVo();
        BeanUtils.copyProperties(apArticle,hotArticleVo);
        //3.最新分值
        Integer score = computeArticleScore(hotArticleVo);
        //4.实时分值权重*3
        score = score * 3;

        //5.更新redis 缓存热点文章  （分类） redis 分类的Key
        replaceDataToRedis(apArticle,score,ArticleConstants.HOT_ARTICLE_PAGE + apArticle.getChannelId());


        //6.推荐中分类缓存热点文章也要更新一下 （推荐分类）
        replaceDataToRedis(apArticle,score,ArticleConstants.HOT_ARTICLE_PAGE + ArticleConstants.DEFAULT_TAG);

    }

    /**
     * 替换redis 热点文章
     * @param apArticle  替换文章对象
     * @param score  分值
     * @param s redis key 值
     */
    private void replaceDataToRedis(ApArticle apArticle, Integer score, String s) {
        //1.从redis 把热点文章获取出来 List<HotArtcle> =  s1
        String s1 = redisTemplate.opsForValue().get(s);
        List<HotArticleVo> articleVos = JSON.parseArray(s1, HotArticleVo.class);
        //2.判断文章再集合中是否存在  更新redis 分值即可
        if(articleVos == null || articleVos.size() < 0){
            return;
        }
        //flag   false 代表集合中已经存在了，true 代表不存在
        boolean flag = true;
        for (HotArticleVo articleVo : articleVos) {
            if(articleVo.getId() == apArticle.getId()){ // 代表存在
                //更新分值到redis 即可
                articleVo.setScore(score);
                flag = false;
                break;
            }
        }
        //3.判断文章不在集合中  判断分值是否大于最后一篇文章的分值
        if(flag){
            //1.热点新闻满了
            if(articleVos.size() >= 30){ //31
                //和最后一篇文章对比分值
                //热点第30名的文章
                HotArticleVo hotArticleVo = articleVos.get(articleVos.size() - 1);
                if(hotArticleVo.getScore() < score){
                    HotArticleVo hav = new HotArticleVo();
                    BeanUtils.copyProperties(apArticle,hav);
                    hav.setScore(score);
                    //替换掉
                    articleVos.set((articleVos.size() - 1),hav);
                }
            }else{
                //2.热点新闻不足30条
                HotArticleVo hav = new HotArticleVo();
                BeanUtils.copyProperties(apArticle,hav);
                hav.setScore(score);
                articleVos.add(hav);
            }

        }


        //重新排个序
        List<HotArticleVo> collect = articleVos.stream()
                .sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).limit(30).collect(Collectors.toList());
        //重新存入到redis 中
        redisTemplate.opsForValue().set(s,JSON.toJSONString(collect));
    }


    /**
     * 更新数据库的点赞...数量
     * @param avsm
     * @return
     */
    private ApArticle updateArticleDb(ArticleVisitStreamMess avsm) {

        //1.根据文章ID查询出文章的信息
        ApArticle apArticle = apArticleMapper.selectById(avsm.getArticleId());
        if(apArticle == null){
            return null;
        }
        //2.收藏更新数量
        apArticle.setCollection(apArticle.getCollection() != null ? apArticle.getCollection()+ avsm.getCollect() : avsm.getCollect() );
        //3.点赞更新数量
        apArticle.setLikes(apArticle.getLikes() !=null ?apArticle.getLikes()+ avsm.getLike() : avsm.getLike() );
        //4.阅读数量更新
        apArticle.setViews(apArticle.getViews() !=null ?apArticle.getViews()+ avsm.getView() : avsm.getView() );
        //5.评论
        apArticle.setComment(apArticle.getComment() !=null ?apArticle.getComment()+ avsm.getComment() : avsm.getComment() );

        //6.更新到数据库中
        apArticleMapper.updateById(apArticle);

        return apArticle;
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 排序+存储
     * @param hotArticleVos
     * @param redisKey
     */
    private void sortAndToRedis(List<HotArticleVo> hotArticleVos, String redisKey){
        //1.集合根据分值排序
        /*Collections.sort(hotArticleVos, new Comparator<HotArticleVo>() {
            @Override
            public int compare(HotArticleVo o1, HotArticleVo o2) {
                int i =o2.getScore() - o1.getScore();
                if(i == 0){
                    i = o1.getTitle().compareTo(o2.getTitle());
                }
                return i;
            }
        });*/
        List<HotArticleVo> collect = hotArticleVos.stream()
                .sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).limit(30).collect(Collectors.toList());
        /*if(collect.size() > 30){
            collect.subList(0,30);
        }*/
        //2.存储到redis 中
        redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(collect));

    }

    @Autowired
    private IWmUserClient iWmChannelClient;

    /**
     * 计算文章的总分值
     */
    public Integer computeArticleScore(HotArticleVo hotArticleVo){
        //
        Integer score = 0;
        if(hotArticleVo.getViews() != null){
            score+=hotArticleVo.getViews();
        }
        if(hotArticleVo.getLikes() != null){
            score+=(hotArticleVo.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT);
        }
        if(hotArticleVo.getComment() != null){
            score+=(hotArticleVo.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT);
        }
        if(hotArticleVo.getCollection() != null){
            score+=(hotArticleVo.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT);
        }
        return score;
    }

}
