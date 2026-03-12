package com.team1.mvp_test.domain.step.service

import com.team1.mvp_test.infra.s3.s3service.S3ServiceImpl
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class StepTestConfig {
    @Bean
    fun s3Service(): S3ServiceImpl {
        return Mockito.mock(S3ServiceImpl::class.java)
    }

}