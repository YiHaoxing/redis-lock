package com.demo.controller;

import com.demo.annotation.RedisLock;
import com.demo.redis.RedisLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author YiHaoXing
 * @version 1.0.0
 * @className com.demo.controller.A
 * @description 测试Redis锁
 * @date 2019/6/30 22:45
 */
@RestController
@Slf4j
public class LockController {
    @Autowired
    private RedisLockUtils redisLockUtils;
    /**
     * 打开两个窗口,访问t1后立刻访问t2
     * http://localhost:8080/t1/F
     * http://localhost:8080/t2/F
     * t1获取到锁后.t2请求到达.
     * t1会睡眠10S,则t2会一直阻塞直到t1释放锁,t2成功获取锁为止.
     * 这就是lock()方法.获取锁不可中断,线程会阻塞直到成功获取锁.
     */
    @GetMapping("/t1/{key}")
    public String t1(@PathVariable String key) throws InterruptedException {
        redisLockUtils.getLock(key, 30, TimeUnit.SECONDS);
        log.info("Thread:{}获取锁成功",Thread.currentThread().getId());
        log.info("Thread:{}执行业务逻辑中...",Thread.currentThread().getId());
        Thread.sleep(10000);
        //释放锁
        redisLockUtils.unlock(key);
        log.info("Thread:{}释放锁",Thread.currentThread().getId());
        return "t1 over";
    }
    @GetMapping("/t2/{key}")
    public String t2(@PathVariable String key){
        redisLockUtils.getLock(key, 30, TimeUnit.SECONDS);
        log.info("Thread:{}获取锁成功",Thread.currentThread().getId());
        log.info("Thread:{}执行业务逻辑中...",Thread.currentThread().getId());
        //释放锁
        redisLockUtils.unlock(key);
        log.info("Thread:{}释放锁",Thread.currentThread().getId());
        return "t2 over";
    }

    /**
     * 打开两个窗口,访问t3后立刻访问t4
     * http://localhost:8080/t3/T
     * http://localhost:8080/t4/T
     *
     * 首先t3或先获取到锁.然后t4的请求到达.
     * 此时t3在睡眠.因此还没有释放锁.t4未能获取到锁.
     * t4获取锁时设置了5S等待时间.等待5S后,t3已经释放了锁.此时t4可以成功获取到锁.
     * 假设t4获取锁设置的等待时间为1S.则等待时间到时,t3仍未释放锁.此时t4无法获取到锁.则获取锁失败.
     * 这就是tryLock()方法.
     */
    @GetMapping("/t3/{key}")
    public String t3(@PathVariable String key){
        boolean lock = false;
        try {
            //等待时间5S.缓存过期时间30S
            lock = redisLockUtils.getReentrantLock(key, 5, 30, TimeUnit.SECONDS);
            if(lock){
                //do something.
                log.info("Thread:{}获取锁成功",Thread.currentThread().getId());
                log.info("Thread:{}执行业务逻辑中...",Thread.currentThread().getId());
                Thread.sleep(5000);
            }else {
                log.info("Thread:{}获取锁失败",Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //释放锁
            if(lock){
                redisLockUtils.unlock(key);
                log.info("Thread:{}释放锁",Thread.currentThread().getId());
            }
        }
        return "t3 over";
    }
    @GetMapping("/t4/{key}")
    public String t4(@PathVariable String key){
        boolean lock = false;
        try {
            //等待时间5S.缓存过期时间30S
            lock = redisLockUtils.getReentrantLock(key, 5, 30, TimeUnit.SECONDS);
            if(lock){
                //do something.
                log.info("Thread:{}获取锁成功",Thread.currentThread().getId());
                log.info("Thread:{}执行业务逻辑中...",Thread.currentThread().getId());
                Thread.sleep(5000);
            }else {
                log.info("Thread:{}获取锁失败",Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //释放锁
            if(lock){
                redisLockUtils.unlock(key);
                log.info("Thread:{}释放锁",Thread.currentThread().getId());
            }
        }
        return "t4 over";
    }


    public static final String LOCK_KEY = "T";
    /**
     * @author YiHaoXing
     * @description 通过注解加锁
     * @date 2019/9/5 0:47
     * @param []
     * @return java.lang.String
     **/
    @GetMapping("/t5")
    @RedisLock(LOCK_KEY)
    public String test3(){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "t5 over";
    }
    @GetMapping("/t6")
    @RedisLock(LOCK_KEY)
    public String test4(){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "t6 over";
    }



    @GetMapping("/t7/{key}")
    public String t7(@PathVariable String key){
        boolean lock = false;
        boolean lock2 = false;
        try {
            //等待时间3S.缓存过期时间30S
            lock = redisLockUtils.getReentrantLock(key, 3, 30, TimeUnit.SECONDS);
            if(lock){
                //do something.
                log.info("Thread:{}获取锁成功",Thread.currentThread().getId());
                log.info("Thread:{}执行业务逻辑中...",Thread.currentThread().getId());
                //再次获取锁
                lock2 = redisLockUtils.getReentrantLock(key, 3, 30, TimeUnit.SECONDS);
                if(lock2){
                    log.info("Thread:{}当前线程内再次获取锁",Thread.currentThread().getId());
                }
                Thread.sleep(10000);
            }else {
                log.info("Thread:{}获取锁失败",Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(lock2){
                redisLockUtils.unlock(key);
                log.info("Thread:{}释放锁",Thread.currentThread().getId());
            }
            //释放锁
            if(lock){
                redisLockUtils.unlock(key);
                log.info("Thread:{}释放锁",Thread.currentThread().getId());
            }
        }
        return "t7 over";
    }

}
