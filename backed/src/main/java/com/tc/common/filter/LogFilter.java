// package com.tc.common.filter;
//
// import com.tc.modules.entity.TLog;
// import com.tc.modules.mapper.TLogMapper;
// import com.tc.common.utils.TokenUtils;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.commons.lang3.StringUtils;
// import org.springframework.core.annotation.Order;
// import org.springframework.stereotype.Component;
//
// import javax.annotation.Resource;
// import javax.servlet.*;
// import javax.servlet.annotation.WebFilter;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.io.IOException;
// import java.util.Date;
//
// /**
//  * log filter
//  *
//  * @author sunchao
//  */
// @Slf4j
// @Component
// @WebFilter(filterName = "logFilter", urlPatterns = "/*")
// @Order(-100)
// public class LogFilter implements Filter {
//
//     @Resource
//     private TLogMapper tLogMapper;
//
//     @Override
//     public void init(FilterConfig filterConfig) throws ServletException {
//         Filter.super.init(filterConfig);
//     }
//
//     @Override
//     public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//         HttpServletRequest request = (HttpServletRequest) servletRequest;
//         HttpServletResponse response = (HttpServletResponse) servletResponse;
//         String requestPath = request.getRequestURI();
//         // 记录日志
//         if (!"GET".equals(request.getMethod())) {
//             String token = request.getHeader("token");
//             if (StringUtils.isNotEmpty(token)) {
//                 TLog tLog = new TLog();
//                 tLog.setUserId(Integer.valueOf(TokenUtils.getUserId(token)));
//                 tLog.setRequestPath(requestPath);
//                 tLog.setTargetServer(servletRequest.getLocalAddr());
//                 tLog.setIpAddress(servletRequest.getServerName());
//                 tLog.setRequestTime(new Date());
//                 tLog.setRequestData("");
//                 tLog.setResponseCode(response.getStatus());
//                 tLogMapper.insert(tLog);
//             }
//         }
//         filterChain.doFilter(request, response);
//     }
//
//     @Override
//     public void destroy() {
//         Filter.super.destroy();
//     }
//
// }
