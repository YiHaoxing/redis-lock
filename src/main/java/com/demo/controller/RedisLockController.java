package com.demo.controller;

import com.demo.annotation.RedisLock;
import com.demo.redis.RedisLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author YiHaoXing
 * @version 1.0.0
 * @className com.test.demo.controller.RedisLockController
 * @description
 * @date 2019/6/28 23:47
 */
@RestController
@Slf4j
public class RedisLockController {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RedisLockUtils redisLockUtils;

    /**
     * 打开两个窗口,访问a1后立刻访问a2
     * http://localhost:8080/a1/T
     * http://localhost:8080/a2/T
     * a1先拿到锁.然后睡眠5S.
     * 此时a2请求到达则获取不到锁.
     * 如果5S后,a1已经释放锁.此时a2到达则成功获取到锁.
     */
    @GetMapping("/a1/{key}")
    public String test1(@PathVariable String key){

        String value = new StringBuilder().append(Thread.currentThread().getId()).append(Math.random()).toString();
        boolean lock = false;
        try {
            lock = redisLockUtils.getLockByLua(key, value, 30);
            if(lock){
                log.info("Thread:{}获取锁成功",Thread.currentThread().getId());
                log.info("Thread:{}执行业务逻辑中...",Thread.currentThread().getId());
                Thread.sleep(5000);
            } else {
                log.info("Thread:{}获取锁失败",Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(lock){
                redisLockUtils.releaseLockByLua(key, value);
                log.info("Thread:{}释放锁",Thread.currentThread().getId());
            }
        }
        return "a1 over";
    }
    @GetMapping("/a2/{key}")
    public String test2(@PathVariable String key){

        String value = new StringBuilder().append(Thread.currentThread().getId()).append(Math.random()).toString();
        boolean lock = false;
        try {
            lock = redisLockUtils.getLockByLua(key, value, 30);
            if(lock){
                log.info("Thread:{}获取锁成功",Thread.currentThread().getId());
                log.info("Thread:{}执行业务逻辑中...",Thread.currentThread().getId());
                Thread.sleep(5000);
            } else {
                log.info("Thread:{}获取锁失败",Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(lock){
                redisLockUtils.releaseLockByLua(key, value);
                log.info("Thread:{}释放锁",Thread.currentThread().getId());
            }
        }
        return "a2 over";
    }


    public static final String LOCK_KEY = "T";
    /**
     * @author YiHaoXing
     * @description 通过注解加锁
     * @date 2019/9/5 0:47
     * @param []
     * @return java.lang.String
     **/
    @GetMapping("/a3")
    @RedisLock(LOCK_KEY)
    public String test3(){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "a3 over";
    }
    @GetMapping("/a4")
    @RedisLock(LOCK_KEY)
    public String test4(){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "a4 over";
    }
}
