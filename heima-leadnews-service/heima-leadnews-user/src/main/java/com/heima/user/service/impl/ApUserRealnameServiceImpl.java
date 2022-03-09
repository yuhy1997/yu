package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.apis.wemedia.IWmUserClient;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.ApUserRealnameDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.ApUserRealnameMapper;
import com.heima.user.service.ApUserRealnameService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ApUserRealnameServiceImpl extends ServiceImpl<ApUserRealnameMapper,ApUserRealname> implements ApUserRealnameService {
    @Override
    public ResponseResult list(ApUserRealnameDto dto) {
        //1.校验参数
        if(null == dto){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //1.1 校验一个分页参数
        dto.checkParam();
        //2.分页查询
        //状态
        //            0 创建中
        //            1 待审核
        //            2 审核失败
        //            9 审核通过
        IPage<ApUserRealname> pageInfo = new Page<>(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<ApUserRealname> wrapper = new LambdaQueryWrapper<>();
        if(null != dto.getStatus()){ //添加查询条件
            wrapper.eq(ApUserRealname::getStatus,dto.getStatus());
        }
        IPage<ApUserRealname> page = page(pageInfo, wrapper);
        //3.封装返回结果
        PageResponseResult prr = new PageResponseResult((int)page.getCurrent(),(int)page.getSize(),(int)page.getTotal());
        prr.setData(page.getRecords());
        prr.setCode(200);
        prr.setErrorMessage("查询成功");
        return prr;
    }

    @Override
    public ResponseResult authFail(ApUserRealnameDto dto) {
        //1.校验参数
        if(null == dto){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.修改状态，并且添加拒绝原因即可
        ApUserRealname realname = getById(dto.getId());
        //3.判断是否存在此信息
        if(realname == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //4.修改
        //决绝状态码
        realname.setStatus((short)2);
        //拒绝原因
        realname.setReason(dto.getMsg());
        //修改更新时间
        realname.setUpdatedTime(new Date());
        updateById(realname);
        return ResponseResult.okResult("操作成功");
    }

    @Override
    public ResponseResult authPass(ApUserRealnameDto dto) {
        //1.校验参数
        if(null == dto){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.修改状态，并且添加拒绝原因即可
        ApUserRealname realname = getById(dto.getId());
        //3.判断是否存在此信息
        if(realname == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //4.修改
        //决绝状态码
        realname.setStatus((short)9);
        //修改更新时间
        realname.setUpdatedTime(new Date());
        updateById(realname);

        //todo 给这个APP用户开通自媒体账号
        ApUser apUser = apUserMapper.selectById(realname.getUserId());

        //5.封装WMUser对象
        WmUser wmUser = new WmUser();
        BeanUtils.copyProperties(apUser,wmUser);
        //6.补全WmUser数据
        wmUser.setCreatedTime(new Date());
        wmUser.setStatus(9);
        //如何不适用feign 降低两个服务间得耦合度呢？
        //spring  核心  解耦合（类于类指尖得耦合）
        //消息队列 RabbitMQ   ActiveMq   RocketMQ   kafka(快 -- 大数据开发)  30W/S 服务间得耦合度呢
        //iWmUserClient.addWmUser(wmUser); //耦合过高
        return ResponseResult.okResult("操作成功");
    }

    @Autowired
    private IWmUserClient iWmUserClient;

    @Autowired
    private ApUserMapper apUserMapper;
}
