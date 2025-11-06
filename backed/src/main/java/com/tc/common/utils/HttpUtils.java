package com.tc.common.utils;

import com.tc.common.spring.SpringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class HttpUtils {

    /**
     * post 请求 application/x-www-form-urlencoded.
     * @param url 请求URL
     * @param params 请求参数
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> sendPostFrom(String url, Map<String, String> params, Class<T> responseType) {
        RestTemplate restTemplate = SpringUtils.getBean(RestTemplate.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        // 组装参数
        if (params != null) {
            for (String key : params.keySet()) {
                map.add(key, params.get(key));
            }
        }
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForEntity(url, request, responseType);
    }

    public static <T> ResponseEntity<T> sendPostForm(String url, Map<String, String> customHeaders, Map<String, String> params, Class<T> responseType) {
        RestTemplate restTemplate = SpringUtils.getBean(RestTemplate.class);
        HttpHeaders headers = new HttpHeaders();

        // 默认的 Content-Type
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 如果传入了自定义 headers，进行覆盖或追加
        if (customHeaders != null) {
            for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
                headers.set(entry.getKey(), entry.getValue());
            }
        }

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        // 组装参数
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                map.add(entry.getKey(), entry.getValue());
            }
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForEntity(url, request, responseType);
    }




    /**
     * POST请求调用方式
     *
     * @param url 请求URL
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> sendPostJson(String url, Object requestBody, Class<T> responseType) {
        RestTemplate restTemplate = SpringUtils.getBean(RestTemplate.class);
        return restTemplate.postForEntity(url, requestBody, responseType);
    }

    /**
     * 自定义请求头和请求体的POST请求调用方式
     *
     * @param url           请求URL
     * @param requestEntity 请求头和请求体封装对象
     * @param responseType  返回对象类型
     * @param uriVariables  URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> post(String url, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {
        RestTemplate restTemplate = SpringUtils.getBean(RestTemplate.class);
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType, uriVariables);
    }

    /**
     * 带请求头的POST请求调用方式
     *
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public  static <T> ResponseEntity<T> post(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(requestBody, headers);
        return post(url, requestEntity, responseType, uriVariables);
    }

    /**
     * 带请求头的POST请求调用方式
     *
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> post(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAll(headers);
        return post(url, httpHeaders, requestBody, responseType, uriVariables);
    }


    /**
     * GET请求调用方式
     *
     * @param url 请求URL
     * @param responseType 返回对象类型
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> sendGet(String url, Class<T> responseType) {
        RestTemplate restTemplate = SpringUtils.getBean(RestTemplate.class);
        return restTemplate.getForEntity(url, responseType);
    }

    /**
     * get 请求
     *
     * @param url 请求路径
     * @return
     */
    public static <T> ResponseEntity<T> sendGet(String url, Map<String, String> headerMap, Class<T> responseBodyType) {
        RestTemplate restTemplate = SpringUtils.getBean(RestTemplate.class);
        HttpHeaders headers = new HttpHeaders();
        if (headerMap != null) {
            for (String key : headerMap.keySet()) {
                headers.add(key, headerMap.get(key));
            }
        }
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseBodyType);
    }
}
