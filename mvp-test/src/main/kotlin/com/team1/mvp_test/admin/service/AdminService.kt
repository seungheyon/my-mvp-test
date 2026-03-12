package com.team1.mvp_test.admin.service

import com.team1.mvp_test.admin.dto.adminauthority.MvpTestListResponse
import com.team1.mvp_test.admin.dto.adminauthority.RejectRequest
import com.team1.mvp_test.common.error.AdminErrorMessage
import com.team1.mvp_test.common.error.CategoryErrorMessage
import com.team1.mvp_test.common.exception.ModelNotFoundException
import com.team1.mvp_test.domain.category.model.Category
import com.team1.mvp_test.domain.category.repository.CategoryRepository
import com.team1.mvp_test.domain.enterprise.model.EnterpriseState
import com.team1.mvp_test.domain.enterprise.repository.EnterpriseRepository
import com.team1.mvp_test.domain.member.model.MemberState
import com.team1.mvp_test.domain.member.repository.MemberRepository
import com.team1.mvp_test.domain.member.repository.MemberTestRepository
import com.team1.mvp_test.domain.mvptest.model.MvpTestState
import com.team1.mvp_test.domain.mvptest.repository.MvpTestRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
    private val mvpTestRepository: MvpTestRepository,
    private val enterpriseRepository: EnterpriseRepository,
    private val memberRepository: MemberRepository,
    private val memberTestRepository: MemberTestRepository,
    private val categoryRepository: CategoryRepository,
) {

    @Transactional
    fun approveMvpTest(testId: Long) {
        val mvpTest = mvpTestRepository.findByIdOrNull(testId)
            ?: throw ModelNotFoundException("MvpTest", testId)
        if (mvpTest.state == MvpTestState.APPROVED) throw IllegalStateException(AdminErrorMessage.ALREADY_APPROVED_TEST.message)
        mvpTest.approve()
    }

    @Transactional
    fun rejectMvpTest(testId: Long, request: RejectRequest) {
        val mvpTest = mvpTestRepository.findByIdOrNull(testId)
            ?: throw ModelNotFoundException("MvpTest", testId)
        if (mvpTest.state == MvpTestState.REJECTED) throw IllegalStateException(AdminErrorMessage.ALREADY_REJECTED_TEST.message)
        mvpTest.reject(request.reason)
    }

    @Transactional
    fun approveEnterprise(enterpriseId: Long) {
        val enterprise = enterpriseRepository.findByIdOrNull(enterpriseId)
            ?: throw ModelNotFoundException("Enterprise", enterpriseId)
        if (enterprise.state == EnterpriseState.APPROVED) throw IllegalStateException(AdminErrorMessage.ALREADY_APPROVED_ENTERPRISE.message)
        enterprise.approve()
    }

    @Transactional
    fun rejectEnterprise(enterpriseId: Long, request: RejectRequest) {
        val enterprise = enterpriseRepository.findByIdOrNull(enterpriseId)
            ?: throw ModelNotFoundException("Enterprise", enterpriseId)
        if (enterprise.state == EnterpriseState.REJECTED) throw IllegalStateException(AdminErrorMessage.ALREADY_REJECTED_ENTERPRISE.message)
        enterprise.reject(request.reason)
    }

    @Transactional
    fun blockEnterprise(enterpriseId: Long, request: RejectRequest) {
        val enterprise = enterpriseRepository.findByIdOrNull(enterpriseId)
            ?: throw ModelNotFoundException("Enterprise", enterpriseId)
        if (enterprise.state == EnterpriseState.REJECTED) throw IllegalStateException(AdminErrorMessage.ALREADY_REJECTED_ENTERPRISE.message)
        if (enterprise.state == EnterpriseState.BLOCKED) throw IllegalStateException(AdminErrorMessage.ALREADY_BLOCKED_ENTERPRISE.message)
        enterprise.block(request.reason)
    }

    @Transactional
    fun approveMember(memberId: Long) {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw ModelNotFoundException("Member", memberId)
        if (member.state == MemberState.ACTIVE) throw IllegalStateException(AdminErrorMessage.ALREADY_APPROVED_MEMBER.message)
        member.active()
    }

    @Transactional
    fun blockMember(memberId: Long, request: RejectRequest) {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw ModelNotFoundException("Member", memberId)
        if (member.state == MemberState.BLOCKED) throw IllegalStateException(AdminErrorMessage.ALREADY_BLOCKED_MEMBER.message)
        member.block(request.reason)
    }

    fun getMemberMvpTest(memberId: Long): List<MvpTestListResponse> {
        val memberTest = memberTestRepository.findAllByMemberId(memberId)
            ?: throw ModelNotFoundException("Member", memberId)
        return memberTest.map { MvpTestListResponse.from(it) }
    }

    fun createCategory(category: String) {
        if (categoryRepository.existsByName(category)) throw IllegalStateException(CategoryErrorMessage.ALREADY_EXIST.message)
        categoryRepository.save(Category(name = category)).name
    }


}