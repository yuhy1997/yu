package com.heima.comment.service.impl;

import com.heima.comment.pojos.ApComment;
import com.heima.comment.pojos.ApCommentLike;
import com.heima.comment.pojos.ApCommentRepay;
import com.heima.comment.service.CommentService;
import com.heima.comment.vo.ApCommentVo;
import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.comment.dtos.CommentLikeDto;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public ResponseResult save(CommentSaveDto dto) {
        //1.检查参数
        if(dto == null || StringUtils.isEmpty(dto.getContent()) ){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
       /* ApUser user = ApUserThreadLocalUtil.getUser();
        if(user == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }*/
        //2.构建存储得POJO
        ApComment apComment = new ApComment();
        apComment.setContent(dto.getContent());
        apComment.setEntryId(dto.getArticleId());
        apComment.setCreatedTime(new Date());
        apComment.setType(0);
        apComment.setFlag(0);
        apComment.setReply(0);
        apComment.setLikes(0);
        //品论人名字和ID
        apComment.setAuthorId(1001);
        //品论人名字
        apComment.setAuthorName("xiaosan");
        //3.存储到Mongo 中
        mongoTemplate.save(apComment);
        return ResponseResult.okResult("评论成功");
    }

    @Override
    public ResponseResult load(CommentDto dto) {
        //1.检查参数
        if(dto == null ){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.根据条件查询 文章ID  根据评论时间倒叙查询
        Query query = new Query();
        //分页查询
        query.limit(10);
        query.addCriteria(Criteria.where("entryId").is(dto.getArticleId()));
        //根据时间倒叙排序
        query.with(Sort.by(Sort.Direction.DESC,"createdTime"));
        if(dto.getIndex() != 1){
            query.addCriteria(Criteria.where("createdTime").lt(dto.getMinDate()));
        }
        //operation
        List<ApComment> apComments = mongoTemplate.find(query, ApComment.class);
        //便利所有的评论，看看此用户是否给这个文章点过赞了
        //构建一个VO的数组用来存储 apCommentVo
        List<ApCommentVo> apCommentVos = new ArrayList<>();
        for (ApComment apComment : apComments) {
            Query query1 = new Query(Criteria.where("commentId").is(apComment.getId()));
            query1.addCriteria(Criteria.where("authorId").is(1002));
            List<ApCommentLike> apCommentLikes = mongoTemplate.find(query1, ApCommentLike.class);
            ApCommentVo apCommentVo = new ApCommentVo();
            BeanUtils.copyProperties(apComment,apCommentVo);
            if(apCommentLikes.size() > 0){
                apCommentVo.setOperation(0);
            }
            apCommentVos.add(apCommentVo);
        }

        return ResponseResult.okResult(apCommentVos);
    }

    @Override
    public ResponseResult like(CommentLikeDto dto) {
        //1.检查参数
        if(dto == null ){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        /* ApUser user = ApUserThreadLocalUtil.getUser();
        if(user == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }*/

        //2.判断是点赞还是取消点赞
        if(dto.getOperation() == 0){//点赞
            //3.点赞 ： 把点赞信息存储到Mongo 中
            ApCommentLike acl = new ApCommentLike();
            acl.setCommentId(dto.getCommentId());
            //假装写死
            acl.setAuthorId(1002);
            mongoTemplate.save(acl);
        }else{
            //4.取消点赞： 把点赞信息从Mongo中删除
            Query query = new Query(Criteria.where("commentId").is(dto.getCommentId()));
            query.addCriteria(Criteria.where("authorId").is(1002));
            mongoTemplate.remove(query,ApCommentLike.class);
        }
        //5.对文章评论表数据的点赞操作进行维护  +1   -1
        ApComment apComment = mongoTemplate.findById(dto.getCommentId(), ApComment.class);

        apComment.setLikes(apComment.getLikes()+(dto.getOperation() == 0 ?1:-1));

        //6.更新到Mongo中
        mongoTemplate.save(apComment);

        return ResponseResult.okResult("操作成功");
    }

    /**
     * 根据文章ID删除文章的评论信息和回复信息
     * @param id
     * @return
     */
    @Override
    public ResponseResult removeById(long id) {

        //1.获得文章的所有评论的ID
        Query query = new Query(Criteria.where("entryId").is(id));
        List<ApComment> apComments = mongoTemplate.find(query, ApComment.class);

        //1.2 获得集合中的所有ID
        List<String> ids = apComments.stream().map(c -> c.getId()).collect(Collectors.toList());

        //2.先删除回复
        Query query2 = new Query(Criteria.where("commentId").in(ids));
        mongoTemplate.remove(query2,ApCommentRepay.class);

        //3.删除评论 （点赞不处理）
        Query query3 = new Query(Criteria.where("entryId").is(id));
        mongoTemplate.remove(query3,ApComment.class);

        return ResponseResult.okResult("操作成功");
    }
}
