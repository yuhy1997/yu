package com.heima.user.interceptor;

import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.utils.thread.ApUserThreadLocalUtil;
import com.heima.utils.thread.WmThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户请求拦截器  存放自媒体用户id
 * 使用：必须添加的到springmvc容器当中才能后进行使用 WebMvcConfig
 */
public class ApTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private ApUserMapper apUserMapper;

    //请求前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //1.从请求头中获得userid
        String userId = request.getHeader("userId");

        if(request.getRequestURI().contains("login")){
            //登录请求不需要拦截直接放行
            return true;
        }

        if(request.getRequestURI().contains("getUser")){
            //登录请求不需要拦截直接放行
            return true;
        }

        if(StringUtils.isEmpty(userId)){
            System.out.println("请先登录");
            return false;
        }
        //如果是登录请求直接放行
        //2.存储到ThreadLocal中
        ApUser apUser = new ApUser();
        ApUser apUser1 = apUserMapper.selectById(Integer.parseInt(userId));
        apUser.setId(Integer.parseInt(userId));
        apUser.setName(apUser1.getName());
        ApUserThreadLocalUtil.setUser(apUser);
        //放行即可
        return true;
    }
    //请求后
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

    }
    //请求结束
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        //清理掉线程对象中的数据
        WmThreadLocalUtil.clear();
    }
}
