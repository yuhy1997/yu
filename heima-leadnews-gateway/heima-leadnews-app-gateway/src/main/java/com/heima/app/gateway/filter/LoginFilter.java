package com.heima.app.gateway.filter;

import com.heima.app.gateway.utils.AppJwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

/**
 * 登录拦截器
 */
@Component
public class LoginFilter implements GlobalFilter , Ordered {
    //请求拦截操作
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.获得用户的请求URI
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String uri = request.getURI().getPath();
        //2.判断是否是登录请求，如果是直接放行请求即可
        if(uri.contains("/login")){//是登录请求
            //放行接口
            return chain.filter(exchange);
        }
        //3.如果不是登录请求 ，获得用户的token
        HttpHeaders headers = request.getHeaders();
        List<String> tokens = headers.get("token");
        //4.token不存在，返回401 权限不足
        if(tokens == null || tokens.size() <= 0){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //5.如果token存在，判断token 是否失效
        String jwt = tokens.get(0);
        try {
            Claims claimsBody = AppJwtUtil.getClaimsBody(jwt);

            //把用户的id 存储在请求的header 头中
            Object userId = claimsBody.get("id");
            //把令牌获取到的id 存储到请求头
            //在header中添加新的信息
            ServerHttpRequest serverHttpRequest = request.mutate().headers(httpHeaders -> {
                httpHeaders.add("userId", userId + "");
            }).build();
            //把原有的头信息数据重置掉
            exchange.mutate().request(serverHttpRequest).build();

        }catch (Exception e){
            //6.假令牌，返回401 权限不足
            System.out.println("是个假令牌");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //7.真令牌，放行请求即可
        return chain.filter(exchange);
    }

    /**
     * 过滤器执行顺序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
