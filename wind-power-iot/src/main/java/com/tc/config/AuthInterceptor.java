package com.tc.config;

import com.alibaba.fastjson.JSON;
import com.tc.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {


    @Value("${auth.api-key}")
    private String API_KEY;

    @Value("${auth.api-secret}")
    private String API_SECRET;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        if (handler instanceof HandlerMethod) {
            String apiKey = request.getHeader(API_KEY);
            if (StringUtils.hasText(apiKey) && API_SECRET.equals(apiKey)) {
                return true;
            } else {
                log.warn("Unauthorized access attempt with invalid {}", API_KEY);
                Result<?> result = Result.error("Unauthorized access attempt with invalid " + API_KEY);
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().print(JSON.toJSONString(result));
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        }
        return true;
    }

}
