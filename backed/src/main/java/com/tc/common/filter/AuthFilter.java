package com.tc.common.filter;

import com.tc.modules.entity.TUser;
import com.tc.modules.service.TUserService;
import com.tc.common.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * @author sunchao
 * @date 2024/2/27 13:45
 */
@Slf4j
@Component
@WebFilter(filterName = "authFilter", urlPatterns = "/*")
@Order(-1000)
public class AuthFilter implements Filter {

    @Autowired
    private GatewaySetting gatewaySetting;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.redis.prefix}")
    private String prefix;

    @Resource
    private TUserService userService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(request);
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // log.info(request.getRequestURI());
        if (request.getRequestURI().contains("/swagger-ui")
                || request.getRequestURI().contains("/swagger-resources")
                || request.getRequestURI().startsWith("/open")
                || request.getRequestURI().startsWith("/websocket")
                || request.getRequestURI().startsWith("/sse")
                || request.getRequestURI().contains("/doc")
                || request.getRequestURI().contains("/webjars")
                || request.getRequestURI().startsWith("/audio/stream")
        ) {
            chain.doFilter(request, response);
            return;
        }

        if (gatewaySetting.getWhiteUrls().contains(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("token");
        if(("GET").equals(request.getMethod()) && StringUtils.isEmpty(token)){
            token = request.getParameter("token");
        }

        if(StringUtils.isEmpty(token)){
            // header和param中均未携带token
            response.setStatus(200);
            response.addHeader("Content-Type", "application/json; charset=utf-8");
            String message = "{\"code\":3002,\"message\":\"请登录后尝试\"}";
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(message.getBytes());
            outputStream.flush();
            return;
        }
        String userId = validateToken(token);
        if (userId == null) {
            // token失效 拒绝请求
            response.setStatus(200);
            response.addHeader("Content-Type", "application/json; charset=utf-8");
            String message = "{\"code\":2002,\"message\":\"Token失效\"}";
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(message.getBytes());
            outputStream.flush();
            return;
        }
        TUser user = userService.getById(userId);
        if (user.getPasswordInvalidTime().before(new Date()) && request.getRequestURI().indexOf("modifyPassword") == -1) {
            // 密码已过期
            response.setStatus(200);
            response.addHeader("Content-Type", "application/json; charset=utf-8");
            String message = "{\"code\":2001,\"message\":\"密码过期\"}";
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(message.getBytes());
            outputStream.flush();
            return;
        }
        requestWrapper.addHeader("userId", user.getId().toString());
        requestWrapper.addHeader("userName", user.getUserName());
        chain.doFilter(requestWrapper, response);
    }

    public String validateToken(String token) {
        String userId = TokenUtils.getUserId(token);
        if (userId == null || redisTemplate.opsForValue().get(prefix + userId) == null || !redisTemplate.opsForValue().get(prefix + userId).equals(token)) {
            return null;
        }
        return userId;
    }
}

