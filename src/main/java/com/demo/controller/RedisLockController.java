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
 * @description 测试Redis锁.基于Lettuce
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
            //lock = redisLockUtils.getLock(key, value, 30);
            if(lock){
                log.info("获取锁成功,Thread:{}",Thread.currentThread().getId());
                Thread.sleep(5000);
            } else {
                log.info("获取锁失败,Thread:{}",Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(lock){
                redisLockUtils.releaseLockByLua(key, value);
                log.info("释放锁,thread:{}",Thread.currentThread().getId());
            }
        }
        return "thread1 over";
    }
    @GetMapping("/a2/{key}")
    public String test2(@PathVariable String key){

        String value = new StringBuilder().append(Thread.currentThread().getId()).append(Math.random()).toString();
        boolean lock = false;
        try {
            lock = redisLockUtils.getLockByLua(key, value, 30);
            if(lock){
                log.info("获取锁成功,Thread:{}",Thread.currentThread().getId());
                Thread.sleep(5000);
            } else {
                log.info("获取锁失败,Thread:{}",Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(lock){
                redisLockUtils.releaseLockByLua(key, value);
                log.info("释放锁,thread:{}",Thread.currentThread().getId());
            }
        }
        return "thread2 over";
    }
}
