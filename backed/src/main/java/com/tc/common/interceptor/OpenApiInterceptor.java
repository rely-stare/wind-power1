package com.tc.common.interceptor;

import com.alibaba.fastjson.JSON;
import com.tc.common.annotation.OpenApiPermission;
import com.tc.common.http.DataResponse;
import com.tc.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * 开放接口验证拦截器
 */
@Slf4j
@Component
public class OpenApiInterceptor implements HandlerInterceptor {

    private static final String API_KEY = "api-key";

    private static final String API_SECRET = "123456789";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Optional<OpenApiPermission> annotation = Optional.ofNullable(handlerMethod.getMethodAnnotation(OpenApiPermission.class));

            if (annotation.isPresent()) {
                String apiKey = request.getHeader(API_KEY);
                if (StringUtils.isNotEmpty(apiKey) && API_SECRET.equals(apiKey)) {
                    return true;
                } else {
                    log.warn("Unauthorized access attempt with invalid API key");
                    DataResponse<Object> dataResponse = new DataResponse<>(401, "Unauthorized access attempt with invalid API key", null);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().print(JSON.toJSONString(dataResponse));
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return false;
                }
            }
        }
        return true;
    }
}
