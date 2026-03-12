package com.team1.mvp_test.infra.redisson

import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedissonService(
    private val redissonClient: RedissonClient,
) {

    fun getLock(key: String, waitTime: Long, releaseTime: Long): RLock {
        val lock = redissonClient.getLock("Lock:$key")
        if (!lock.tryLock(waitTime, releaseTime, TimeUnit.MILLISECONDS)) {
            throw RuntimeException("Lock 획득 실패!")
        }
        return lock
    }

    fun unlock(lock: RLock) {
        lock.unlock()
    }
}