package com.team1.mvp_test.infra.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfig(
    @Value("\${data.redis.host}") private val redisHost: String,
    @Value("\${data.redis.port}") private val redisPort: Int,
) {
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration(redisHost, redisPort)
        val lettuceClientConfiguration = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(10))
            .shutdownTimeout(Duration.ofMillis(100))
            .build()

        return LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration)
    }

    // @Bean
    // fun sentinelConnectionFactory(): LettuceConnectionFactory {
    //     val sentinelConfig: RedisSentinelConfiguration = RedisSentinelConfiguration()
    //         .master("mymaster")
    //         .sentinel("sentinel1", 26379)
    //         .sentinel("sentinel1", 26379)
    //         .sentinel("sentinel1", 26379)
    //     return LettuceConnectionFactory(sentinelConfig)
    // }

    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        template.connectionFactory = redisConnectionFactory()
        return template
    }

    @Primary
    @Bean("redisCacheManager")
    fun redisCacheManager(): CacheManager {
        val objectMapper = ObjectMapper().apply {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
            )
        }
        val jsonSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        val cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
            )

        return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(cacheConfig)
            .build()
    }
}