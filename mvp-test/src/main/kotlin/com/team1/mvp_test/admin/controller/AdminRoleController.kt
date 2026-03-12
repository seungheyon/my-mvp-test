package com.team1.mvp_test.admin.controller

import com.team1.mvp_test.admin.dto.adminauthority.MvpTestListResponse
import com.team1.mvp_test.admin.dto.adminauthority.RejectRequest
import com.team1.mvp_test.admin.service.AdminService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/admin/role")
class AdminRoleController(
    private val adminService: AdminService
) {

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/enterprises/{enterpriseId}/approve")
    fun approveEnterprise(
        @PathVariable enterpriseId: Long,
    ): ResponseEntity<Unit> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(adminService.approveEnterprise(enterpriseId))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/enterprises/{enterpriseId}/reject")
    fun rejectEnterprise(
        @PathVariable enterpriseId: Long,
        @Valid @RequestBody request: RejectRequest
    ): ResponseEntity<Unit> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(adminService.rejectEnterprise(enterpriseId, request))
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/enterprises/{enterpriseId}/block")
    fun blockEnterprise(
        @PathVariable enterpriseId: Long,
        @Valid @RequestBody request: RejectRequest
    ): ResponseEntity<Unit> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(adminService.blockEnterprise(enterpriseId, request))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/mvp-tests/{testId}/approve")
    fun approveMvpTest(
        @PathVariable testId: Long
    ): ResponseEntity<Unit> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(adminService.approveMvpTest(testId))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/mvp-tests/{testId}/reject")
    fun rejectMvpTest(
        @PathVariable testId: Long,
        @RequestBody request: RejectRequest
    ): ResponseEntity<Unit> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(adminService.rejectMvpTest(testId, request))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/members/{memberId}/mvp-tests")
    fun getMemberMvpTest(
        @PathVariable memberId: Long
    ): ResponseEntity<List<MvpTestListResponse>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(adminService.getMemberMvpTest(memberId))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/members/{memberId}/block")
    fun blockMember(
        @PathVariable memberId: Long,
        @Valid @RequestBody request: RejectRequest
    ): ResponseEntity<Unit> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(adminService.blockMember(memberId, request))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/members/{memberId}/approve")
    fun approveMembers(
        @PathVariable memberId: Long
    ): ResponseEntity<Unit> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(adminService.approveMember(memberId))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/category")
    fun createCategory(
        @RequestParam("category") category: String,
    ): ResponseEntity<Unit> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(adminService.createCategory(category))
    }
}