package com.team1.mvp_test.domain.mvptest.controller

import com.team1.mvp_test.common.dto.CursorPageResponse
import com.team1.mvp_test.domain.mvptest.dto.CreateMvpTestRequest
import com.team1.mvp_test.domain.mvptest.dto.MemberInfoResponse
import com.team1.mvp_test.domain.mvptest.dto.MvpTestResponse
import com.team1.mvp_test.domain.mvptest.dto.UpdateMvpTestRequest
import com.team1.mvp_test.domain.mvptest.service.MvpTestService
import com.team1.mvp_test.infra.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("api/v1/mvp-tests")
class MvpTestController(
    private val mvpTestService: MvpTestService
) {

    @PostMapping
    @PreAuthorize("hasRole('ENTERPRISE')")
    fun createMvpTest(
        @Valid request: CreateMvpTestRequest,
        mainImageFile: MultipartFile,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<MvpTestResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mvpTestService.createMvpTest(userPrincipal.id, request, mainImageFile))
    }

    @PutMapping("/{testId}")
    @PreAuthorize("hasRole('ENTERPRISE')")
    fun updateMvpTest(
        @Valid request: UpdateMvpTestRequest,
        mainImageFile: MultipartFile,
        @PathVariable("testId") testId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<MvpTestResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(mvpTestService.updateMvpTest(userPrincipal.id, testId, request, mainImageFile))
    }

    @DeleteMapping("/{testId}")
    @PreAuthorize("hasRole('ENTERPRISE')")
    fun deleteMvpTest(
        @PathVariable("testId") testId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<Unit> {
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(mvpTestService.deleteMvpTest(userPrincipal.id, testId))
    }

    @GetMapping("/{testId}")
    fun getMvpTest(
        @PathVariable("testId") testId: Long,
    ): ResponseEntity<MvpTestResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(mvpTestService.getMvpTest(testId))
    }

    @GetMapping
    fun getMvpTestList(
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<CursorPageResponse<MvpTestResponse>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(mvpTestService.getMvpTestList(cursor, size))
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/{testId}/apply")
    fun applyToMvpTest(
        @PathVariable("testId") testId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<Unit> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(mvpTestService.applyToMvpTest(userPrincipal.id, testId))
    }

    @GetMapping("/{testId}/member")
    @PreAuthorize("hasRole('ENTERPRISE')")
    fun getTestMemberList(
        @PathVariable("testId") testId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
    ): List<MemberInfoResponse> {
        val enterpriseId = userPrincipal.id
        return mvpTestService.getMemberList(testId, enterpriseId)
    }

    @GetMapping("/enterprise")
    @PreAuthorize("hasRole('ENTERPRISE')")
    fun getMvpTestsByEnterprise(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
    ): List<MvpTestResponse> {
        val enterpriseId = userPrincipal.id
        return mvpTestService.getMvpTestsByEnterprise(enterpriseId)
    }

    @GetMapping("/available-tests")
    @PreAuthorize("hasRole('MEMBER')")
    fun getAvailableTests(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        pageable: Pageable
    ): ResponseEntity<List<MvpTestResponse>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(mvpTestService.getAvailableTests(userPrincipal.id, pageable))
    }
}