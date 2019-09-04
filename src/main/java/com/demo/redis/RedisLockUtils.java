package com.demo.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @param
 * @author YiHaoXing
 * @description 通过redis实现分布式锁工具类
 * @date 18:02 2019/6/30
 * @return
 **/
@Component
@Slf4j
public class RedisLockUtils {

    @Autowired
    private RedissonClient redissonClient;


    /**
     * @author YiHaoXing
     * @description
     * @date 16:53 2019/9/4
     * @param [lockKey, expireTime, timeUnit]
     * @return void
     **/
    public void getLock(String lockKey,int expireTime, TimeUnit timeUnit){
        RLock lock = redissonClient.getLock(lockKey);
        //拿不到锁线程会一直阻塞.直到拿到锁
        //Redisson的这个方法可以做到阻塞线程的效果.这是Lettuce做不到的.
        //Lettuce拿不到锁后,要么return.要么抛异常.而Redisson可以阻塞线程知道拿到锁.
        log.info("Thread:{}正在获取锁...",Thread.currentThread().getId());
        lock.lock(expireTime,timeUnit);
    }

    /**
     * @param [lockKey, value, waitTime, expireTime]
     * @return boolean
     * @author YiHaoXing
     * @description 可重入锁.默认
     * @date 18:16 2019/6/30
     **/
    public boolean getReentrantLock(String lockKey, int waitTime, int expireTime, TimeUnit timeUnit) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.tryLock(waitTime, expireTime, timeUnit);
    }

    /**
     * @param [lockKey, waitTime, expireTime, timeUnit, threadId]
     * @return boolean
     * @author YiHaoXing
     * @description 可重入锁(异步执行)
     * @date 18:33 2019/6/30
     **/
    public boolean getAsyncReentrantLock(String lockKey, int expireTime, TimeUnit timeUnit, Long threadId) {
        RLock lock = redissonClient.getLock(lockKey);
        RFuture<Void> rFuture;
        if (Optional.ofNullable(threadId).isPresent()) {
            rFuture = lock.lockAsync(expireTime, timeUnit, threadId);
            return rFuture.isSuccess();
        } else {
            rFuture = lock.lockAsync(expireTime, timeUnit);
        }
        return null == rFuture ? false : rFuture.isSuccess();
    }

    /**
     * @author YiHaoXing
     * @description 公平锁
     * @date 18:47 2019/6/30
     * @param [lockKey, waitTime, expireTime, timeUnit]
     * @return boolean
     **/
    public boolean getFairLock(String lockKey, int waitTime, int expireTime, TimeUnit timeUnit) throws InterruptedException {
        RLock fairLock = redissonClient.getFairLock(lockKey);
        return fairLock.tryLock(waitTime, expireTime, timeUnit);
    }

    /**
     * @author YiHaoXing
     * @description 公平锁(异步执行)
     * @date 18:48 2019/6/30
     * @param [lockKey, expireTime, timeUnit, threadId]
     * @return boolean
     **/
    public boolean getAsyncFairLock(String lockKey, int expireTime, TimeUnit timeUnit, Long threadId) {
        RLock fairLock = redissonClient.getFairLock(lockKey);
        RFuture<Void> rFuture;
        if (Optional.ofNullable(threadId).isPresent()) {
            rFuture = fairLock.lockAsync(expireTime, timeUnit, threadId);
            return rFuture.isSuccess();
        } else {
            rFuture = fairLock.lockAsync(expireTime, timeUnit);
        }
        return null == rFuture ? false : rFuture.isSuccess();
    }

    /**
     * @author YiHaoXing
     * @description 读写锁
     * @date 18:58 2019/6/30
     * @param [lockKey, waitTime, expireTime, timeUnit, threadId]
     * @return boolean
     **/
    public boolean getReadWriteLock(String lockKey, int waitTime,int expireTime, TimeUnit timeUnit, Long threadId) throws InterruptedException {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockKey);
        //读锁
        //return readWriteLock.readLock().tryLock(waitTime, expireTime, timeUnit);

        //写锁
        return readWriteLock.writeLock().tryLock(waitTime, expireTime, timeUnit);
    }



    /**
     * @param [lockKey]
     * @return void
     * @author YiHaoXing
     * @description 释放锁
     * @date 18:25 2019/6/30
     **/
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        //如果释放锁的时候,redis数据已经因为超时自动清除了.此时会报异常
        //java.lang.IllegalMonitorStateException: attempt to unlock lock, not locked by current thread by node id: 2ca6b4a4-60d1-424d-b131-9f139be12ff4 thread-id: 47
        lock.unlock();
    }




    //===========================
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    /**
     * 成功获取锁返回值
     */
    private static final Long LOCK_SUCCESS = 1L;
    /**
     * 成功释放锁返回值
     */
    private static final Long UNLOCK_SUCCESS = 1L;

    /**
     * @param [lockKey, value, expireTime]
     * @return boolean
     * @author YiHaoXing
     * @description 获取锁, 原子操作
     * @date 0:45 2019/6/29
     **/
    public boolean getLock(String lockKey, String value, int expireTime) {
        return redisTemplate.opsForValue().setIfAbsent(lockKey, value, expireTime, TimeUnit.SECONDS);
    }

    /**
     * @param [lockKey, value]
     * @return boolean
     * @author YiHaoXing
     * @description 释放锁.非原子操作, 不推荐使用该方式释放锁
     * @date 0:44 2019/6/29
     **/
    public boolean releaseLock(String lockKey, String value) {
        if (value.equals(redisTemplate.opsForValue().get(lockKey))) {
            return redisTemplate.delete(lockKey);
        } else {
            return false;
        }
    }


    /**
     * 释放锁的LUA脚本：如果value的值与参数相等,则删除,否则返回0
     */
    public static final String UNLOCK_SCRIPT_LUA = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    /**
     * @param [lockKey, value]
     * @return boolean
     * @author YiHaoXing
     * @description 使用LUA脚本释放锁, 原子操作
     * @date 0:47 2019/6/29
     **/
    public boolean releaseLockByLua(String lockKey, String value) {
        RedisScript<Long> redisScript = new DefaultRedisScript<>(UNLOCK_SCRIPT_LUA, Long.class);
        return UNLOCK_SUCCESS.equals(redisTemplate.execute(redisScript, Collections.singletonList(lockKey), value));
    }

    /**
     * 获取锁的LUA脚本：用setNx命令设置值,并设置过期时间
     */
    public static final String LOCK_SCRIPT_LUA = "if redis.call('setNx',KEYS[1],ARGV[1]) == 1 then return redis.call('expire',KEYS[1],ARGV[2]) else return 0 end";

    /**
     * @param [lockKey, value, expireTime]
     * @return boolean
     * @author YiHaoXing
     * @description 使用LUA脚本获取锁, 原子操作。过期时间单位为秒
     * @date 0:46 2019/6/29
     **/
    public boolean getLockByLua(String lockKey, String value, int expireTime) {
        RedisScript<Long> redisScript = new DefaultRedisScript<>(LOCK_SCRIPT_LUA, Long.class);
        Long execute = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), value, expireTime);
        System.out.println(execute);
        return LOCK_SUCCESS.equals(redisTemplate.execute(redisScript, Collections.singletonList(lockKey), value, expireTime));
    }

}