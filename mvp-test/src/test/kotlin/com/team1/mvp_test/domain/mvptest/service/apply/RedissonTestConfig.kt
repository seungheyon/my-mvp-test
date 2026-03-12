package com.team1.mvp_test.domain.mvptest.service.apply

import com.team1.mvp_test.domain.member.service.MemberService
import com.team1.mvp_test.infra.redisson.RedissonService
import com.team1.mvp_test.infra.s3.s3service.S3ServiceImpl
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.mockito.Mockito
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import redis.embedded.RedisServer


@TestConfiguration
class RedissonTestConfig {
    //embedded redis 초기화
    private var redisServer: RedisServer = RedisServer(6380)

    //embedded redis 서버를 테스트 실행 이전에 열고, 끝나기 전에 닫는 로직
    @PostConstruct
    fun startRedis() {
        redisServer.stop()
        redisServer.start()
    }

    @PreDestroy
    fun stopRedis() {
        redisServer.stop()
    }

    @Bean
    fun redissonTestClient(): RedissonClient {
        val config = Config.fromYAML(ClassPathResource("redisson.yml").inputStream)
        return Redisson.create(config)
    }

    @Bean
    fun redissonService(redissonTestClient: RedissonClient): RedissonService {
        return RedissonService(redissonTestClient)
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