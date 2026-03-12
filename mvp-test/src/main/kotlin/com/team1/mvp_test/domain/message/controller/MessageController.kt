package com.team1.mvp_test.domain.message.controller

import com.team1.mvp_test.domain.message.VerifyCodeResponse
import com.team1.mvp_test.domain.message.service.MessageService
import net.nurigo.sdk.message.response.SingleMessageSentResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/message")
class MessageController(
    private val messageService: MessageService
) {
    @PostMapping("/send-sms")
    fun sendSMS(
        @RequestParam toNumber: String
    ): ResponseEntity<SingleMessageSentResponse> {
        return ResponseEntity.status(HttpStatus.OK).body(messageService.sendSMS(toNumber))
    }

    @PostMapping("/verify-code")
    fun verifyCode(
        @RequestParam phoneNumber: String,
        @RequestParam code: String,
    ): ResponseEntity<VerifyCodeResponse> {
        return ResponseEntity.status(HttpStatus.OK).body(messageService.verifyCode(phoneNumber, code))
    }
}