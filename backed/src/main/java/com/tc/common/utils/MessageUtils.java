package com.tc.common.utils;

import com.tc.common.constant.Constants;
import com.tc.common.redis.RedisCache;
import com.tc.common.spring.SpringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * 获取i18n资源文件
 *
 * @author tetrabot
 */
public class MessageUtils
{
    /**
     * 根据消息键和参数 获取消息 委托给spring messageSource
     *
     * @param code 消息键
     * @param args 参数
     * @return 获取国际化翻译值
     */
    public static String message(String code, Object... args)
    {
        MessageSource messageSource = SpringUtils.getBean(MessageSource.class);
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        Locale locale = LocaleContextHolder.getLocale();
        Object languageObj = redisCache.getCacheObject(Constants.SYS_CONFIG_KEY + "language");
        if(ObjectUtils.isNotEmpty(languageObj)){
            String language = languageObj.toString();
            if("en_US".equals(language)){
                locale = new Locale("en","US");
            }else {
                locale = new Locale("zh", "CN");
            }
        }else{
            locale = new Locale("zh", "CN");
        }
        try{
            return messageSource.getMessage(code, args, locale);
        }catch (Exception e){
            return code;
        }

    }
}
