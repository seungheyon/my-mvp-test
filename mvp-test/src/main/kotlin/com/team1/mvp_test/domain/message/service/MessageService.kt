package com.team1.mvp_test.domain.message.service

import com.team1.mvp_test.domain.message.VerifyCodeResponse
import com.team1.mvp_test.infra.redis.RedisService
import net.nurigo.sdk.NurigoApp.initialize
import net.nurigo.sdk.message.model.Message
import net.nurigo.sdk.message.request.SingleMessageSendingRequest
import net.nurigo.sdk.message.response.SingleMessageSentResponse
import net.nurigo.sdk.message.service.DefaultMessageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class MessageService(
    private val redisService: RedisService,
    @Value("\${coolsms.api.key}") private val messageKey: String,
    @Value("\${coolsms.api.secret}") private val messageSecret: String,
    @Value("\${coolsms.api.domain}") private val messageDomain: String,
    @Value("\${coolsms.fromNumber}") private val fromNumber: String,
) {
    val messageService: DefaultMessageService = initialize(messageKey, messageSecret, messageDomain)

    fun sendSMS(
        toNumber: String
    ): SingleMessageSentResponse? {
        val verificationCode = generateVerificationCode()
        val message = Message(
            from = fromNumber,
            to = toNumber,
            text = "[MVP-TEST] 본인 확인 인증번호는 $verificationCode 입니다."
        )
        val response = messageService.sendOne(SingleMessageSendingRequest(message))
        redisService.saveVerificationCode(toNumber, verificationCode)
        return response
    }

    private fun generateVerificationCode(): String {
        return (100000..999999).random().toString()
    }

    fun verifyCode(phoneNumber: String, code: String): VerifyCodeResponse {
        val savedCode = redisService.getVerificationCode(phoneNumber)
        if (savedCode == code) {
            redisService.saveVerifiedPhoneNumber(phoneNumber)
            return VerifyCodeResponse(state = true)
        }
        return VerifyCodeResponse(state = false)
    }
}