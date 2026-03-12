package com.team1.mvp_test.infra.redisson

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedissonConfig(
    @Value("\${data.redis.host}") private var redisHost: String,
    @Value("\${data.redis.port}") private var redisPort: Int
) {

    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()
        config.useSingleServer().address = "redis://$redisHost:$redisPort"
        return Redisson.create(config)
    }
}