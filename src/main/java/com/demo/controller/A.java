package com.demo.controller;

import com.demo.redis.RedisLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author YiHaoXing
 * @version 1.0.0
 * @className com.demo.controller.A
 * @description TODO
 * @date 2019/6/30 22:45
 */
@RestController
@Slf4j
public class A {
    @Autowired
    private RedisLockUtils redisLockUtils;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/t1/{key}")
    public String thread1(@PathVariable String key){
        try {
            boolean lock = redisLockUtils.getReentrantLock(key, 1000, 30000, TimeUnit.MILLISECONDS);
            if(lock){
                //do something.
                log.info("获取锁成功,Thread:{}",Thread.currentThread().getId());
                Thread.sleep(5000);
            }else {
                log.info("获取锁失败,Thread:{}",Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //释放锁
            redisLockUtils.unlock(key);
            log.info("释放锁,Thread:{}",Thread.currentThread().getId());
        }

        return "thread1";
    }
    @GetMapping("/t2/{key}")
    public String thread2(@PathVariable String key){
        try {
            boolean lock = redisLockUtils.getReentrantLock(key, 5000, 30000, TimeUnit.MILLISECONDS);
            if(lock){
                //do something.
                log.info("获取锁成功,Thread:{}",Thread.currentThread().getId());
                Thread.sleep(5000);
            }else {
                log.info("获取锁失败,Thread:{}",Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //释放锁
            redisLockUtils.unlock(key);
            log.info("释放锁,Thread:{}",Thread.currentThread().getId());
        }

        return "thread2";
    }

    @GetMapping("/g1/{key}")
    public String g1(@PathVariable String key){
        redisLockUtils.getLock(key, 30000, TimeUnit.MILLISECONDS);
        log.info("获取锁成功,Thread:{}",Thread.currentThread().getId());

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //释放锁
        redisLockUtils.unlock(key);
        log.info("释放锁,Thread:{}",Thread.currentThread().getId());
        return "thread1";
    }
    @GetMapping("/g2/{key}")
    public String g2(@PathVariable String key){
        redisLockUtils.getLock(key, 30000, TimeUnit.MILLISECONDS);
        log.info("获取锁成功,Thread:{}",Thread.currentThread().getId());
        //释放锁
        redisLockUtils.unlock(key);
        log.info("释放锁,Thread:{}",Thread.currentThread().getId());
        return "thread1";
    }

    //Redisson是以hash来存储的.因此存储的数据是一个对象.即key对应的value是一个对象
    //使用hash存储是因为节约内存
    //Redisson的做法是,key为我们存的key.对应的value存储一个对象.Redisson自己会生成一个对象属性.例如:fa2b31b2-a5e0-4cd5-aefa-6861343076f4:40 该属性对应的值为1
    //因此我们取的时候应该 这样 hget key 属性值
    //假设key为 a 则 hget a fa2b31b2-a5e0-4cd5-aefa-6861343076f4:40 返回的值为1
    //1是什么意思呢.

    //Redisson可以实现可重入加锁机制的原因，我觉得跟两点有关：
    //1、Redis存储锁的数据类型是 Hash类型
    //2、Hash数据类型的key值包含了当前线程信息。

    //1其实就是可重入锁的count值.参考可重入锁原理.
    //为什么属性值为 fa2b31b2-a5e0-4cd5-aefa-6861343076f4:40
    // 这个属性值是 "guid + 当前线程的ID" 组成.

    //上面这图的意思就是可重入锁的机制，它最大的优点就是相同线程不需要在等待锁，而是可以直接进行相应操作。

    //微服务项目中,一个服务有多个结点.那么反映到这里就是 key a 会

    @GetMapping("/B1/{key}/{value}")
    public String test1(@PathVariable String key, @PathVariable String value){
        try {
            boolean lock = redisLockUtils.getLockByLua(key, value, 30);
            if(lock){
                log.info("获取锁成功,Thread:{}",Thread.currentThread().getId());
                Thread.sleep(5000);
            } else {
                log.info("获取锁失败,Thread:{}",Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boolean result = redisLockUtils.releaseLockByLua(key, value);
            System.out.println(result);
            log.info("释放锁,Thread:{}",Thread.currentThread().getId());
        }
        return "thread1";
    }
    @GetMapping("/B2/{key}/{value}")
    public String test2(@PathVariable String key, @PathVariable String value){
        try {
            boolean lock = redisLockUtils.getLockByLua(key, value, 30);
            if(lock){
                log.info("获取锁成功,Thread:{}",Thread.currentThread().getId());
                    Thread.sleep(5000);
            } else {
                log.info("获取锁失败,Thread:{}",Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boolean result = redisLockUtils.releaseLockByLua(key, value);
            System.out.println(result);
            log.info("释放锁,Thread:{}",Thread.currentThread().getId());
        }
        return "thread2";
    }
}
