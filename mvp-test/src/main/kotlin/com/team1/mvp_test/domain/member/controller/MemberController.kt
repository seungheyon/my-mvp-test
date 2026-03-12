package com.team1.mvp_test.domain.member.controller

import com.team1.mvp_test.admin.dto.adminauthority.MvpTestListResponse
import com.team1.mvp_test.domain.member.dto.MemberResponse
import com.team1.mvp_test.domain.member.dto.MemberUpdateRequest
import com.team1.mvp_test.domain.member.dto.MemberUpdateResponse
import com.team1.mvp_test.domain.member.dto.SignUpInfoRequest
import com.team1.mvp_test.domain.member.service.MemberService
import com.team1.mvp_test.domain.mvptest.service.MvpTestService
import com.team1.mvp_test.infra.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RequestMapping("api/v1/members")
@RestController
class MemberController(
    private val memberService: MemberService,
    private val mvpTestService: MvpTestService
) {
    @PutMapping("/info")
    @PreAuthorize("hasRole('MEMBER')")
    fun updateSignUpInfo(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: SignUpInfoRequest
    ): ResponseEntity<MemberResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(memberService.updateSignUpInfo(userPrincipal.id, request))
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('MEMBER')")
    fun updateMember(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: MemberUpdateRequest
    ): ResponseEntity<MemberUpdateResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(memberService.updateMember(userPrincipal.id, request))
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('MEMBER')")
    fun getMemberProfile(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<MemberResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(memberService.getMemberById(userPrincipal.id))
    }

    @GetMapping("/{memberId}")
    fun getMemberById(
        @PathVariable memberId: Long,
    ): ResponseEntity<MemberResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(memberService.getMemberById(memberId))
    }

    @GetMapping("/mvp-tests")
    @PreAuthorize("hasRole('MEMBER')")
    fun getTests(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<List<MvpTestListResponse>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(mvpTestService.getMvpTestListByMember(userPrincipal.id))
    }
}