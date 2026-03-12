package com.team1.mvp_test.domain.mvptest.service

import com.team1.mvp_test.domain.member.service.MemberService
import com.team1.mvp_test.infra.redisson.RedissonService
import com.team1.mvp_test.infra.s3.s3service.S3ServiceImpl
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class MvpTestTestConfig {

    @Bean
    fun redissonService(): RedissonService {
        return Mockito.mock(RedissonService::class.java)
    }

    @Bean
    fun s3Service(): S3ServiceImpl {
        return Mockito.mock(S3ServiceImpl::class.java)
    }

    @Bean
    fun memberService(): MemberService {
        return Mockito.mock(MemberService::class.java)
    }
}