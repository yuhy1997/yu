package com.heima.wemedia.interceptor;

import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.thread.WmThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户请求拦截器  存放自媒体用户id
 * 使用：必须添加的到springmvc容器当中才能后进行使用 WebMvcConfig
 */
public class WmTokenInterceptor implements HandlerInterceptor {
    //请求前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //1.从请求头中获得userid   没登陆 。Feign远程调用
        String userId = request.getHeader("userId");
        //如果是登录请求直接放行
        String requestURI = request.getRequestURI();
        if(requestURI.contains("login") ){
            System.out.println("login请求，我要放行...");
            return true;
        }

        if(requestURI.contains("channel")){
            System.out.println("channel请求，我要放行...");
            return true;
        }

        if(requestURI.contains("useradd")){
            System.out.println("useradd请求，我要放行...");
            return true;
        }


        if(StringUtils.isEmpty(userId)){
            System.out.println("请先登录一下...");
            return false;
        }
        //2.存储到ThreadLocal中
        WmUser wmUser = new WmUser();
        wmUser.setId(Integer.parseInt(userId));
        WmThreadLocalUtil.setUser(wmUser);
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
