package com.lyj.aicode.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 */
@Target(ElementType.METHOD)     // 注解1：指定这个注解可以用在哪里
@Retention(RetentionPolicy.RUNTIME)  // 注解2：指定这个注解保留到什么时候
public @interface AuthCheck {    // 定义一个名为 AuthCheck 的注解

    /**
     * 必须有某个角色
     */
    String mustRole() default "";  // 注解的属性，默认值为空字符串
}