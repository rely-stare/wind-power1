package com.tc.common.annotation;


import java.lang.annotation.*;

/**
 * 自定义注解 开放接口权限认证
 *
 * @author yinxiyou
 *
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpenApiPermission {


}
