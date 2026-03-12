package com.team1.mvp_test.infra.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, String>
) {
    companion object {
        private const val VERIFIED_PHONE_PREFIX_ = "verified_"
        private const val VERIFICATION_CODE_PREFIX = "code_"
    }

    fun saveVerificationCode(phoneNumber: String, code: String) {
        val key = VERIFICATION_CODE_PREFIX + phoneNumber
        redisTemplate.opsForValue()[key] = code
        redisTemplate.expire(key, 3, TimeUnit.MINUTES)
    }

    fun getVerificationCode(phoneNumber: String): String? {
        val key = VERIFICATION_CODE_PREFIX + phoneNumber
        return redisTemplate.opsForValue()[key]
    }

    fun saveVerifiedPhoneNumber(phoneNumber: String) {
        val key = VERIFIED_PHONE_PREFIX_ + phoneNumber
        redisTemplate.opsForValue()[key] = phoneNumber
        redisTemplate.expire(key, 10, TimeUnit.MINUTES)
    }

    fun getVerifiedPhoneNumber(phoneNumber: String): String? {
        val key = VERIFIED_PHONE_PREFIX_ + phoneNumber
        return redisTemplate.opsForValue()[key]
    }
}