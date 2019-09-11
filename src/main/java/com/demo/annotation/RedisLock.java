package com.demo.annotation;

import java.lang.annotation.*;

/**
 * @author YiHaoXing
 * @description Redis锁注解
 * @date 23:53 2019/6/28
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLock {
    /**
     * 锁的过期时间.以秒为单位
     */
    int expireTime() default 30;

    /**
     * 未获取到锁后等待重试时间.以秒为单位
     */
    int waitTime() default 3;
    /**
     * redis的key
     * @return
     */
    String value() default "";
}
