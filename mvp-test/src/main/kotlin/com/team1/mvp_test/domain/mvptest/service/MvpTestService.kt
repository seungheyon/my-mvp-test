package com.team1.mvp_test.domain.mvptest.service

import com.team1.mvp_test.admin.dto.adminauthority.MvpTestListResponse
import com.team1.mvp_test.common.dto.CursorPageResponse
import com.team1.mvp_test.common.dto.DateCursorPageResponse
import com.team1.mvp_test.common.error.CategoryErrorMessage
import com.team1.mvp_test.common.error.MemberErrorMessage
import com.team1.mvp_test.common.error.MvpTestErrorMessage
import com.team1.mvp_test.common.exception.ModelNotFoundException
import com.team1.mvp_test.common.exception.NoPermissionException
import com.team1.mvp_test.domain.category.repository.CategoryRepository
import com.team1.mvp_test.domain.enterprise.model.EnterpriseState
import com.team1.mvp_test.domain.enterprise.repository.EnterpriseRepository
import com.team1.mvp_test.domain.member.model.Member
import com.team1.mvp_test.domain.member.model.MemberState
import com.team1.mvp_test.domain.member.model.MemberTest
import com.team1.mvp_test.domain.member.model.MemberTestState
import com.team1.mvp_test.domain.member.repository.MemberRepository
import com.team1.mvp_test.domain.member.repository.MemberTestRepository
import com.team1.mvp_test.domain.member.service.MemberService
import com.team1.mvp_test.domain.mvptest.dto.CreateMvpTestRequest
import com.team1.mvp_test.domain.mvptest.dto.MemberInfoResponse
import com.team1.mvp_test.domain.mvptest.dto.MvpTestResponse
import com.team1.mvp_test.domain.mvptest.dto.UpdateMvpTestRequest
import com.team1.mvp_test.domain.mvptest.model.MvpTest
import com.team1.mvp_test.domain.mvptest.model.MvpTestCategoryMap
import com.team1.mvp_test.domain.mvptest.model.MvpTestSortType
import com.team1.mvp_test.domain.mvptest.model.RecruitType
import com.team1.mvp_test.domain.mvptest.repository.MvpTestCategoryMapRepository
import com.team1.mvp_test.domain.mvptest.repository.MvpTestRepository
import com.team1.mvp_test.infra.redisson.RedissonService
import com.team1.mvp_test.infra.s3.s3service.S3Service
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class MvpTestService(
    private val mvpTestRepository: MvpTestRepository,
    private val categoryRepository: CategoryRepository,
    private val mvpTestCategoryMapRepository: MvpTestCategoryMapRepository,
    private val memberRepository: MemberRepository,
    private val memberTestRepository: MemberTestRepository,
    private val redissonService: RedissonService,
    private val s3Service: S3Service,
    private val memberService: MemberService,
    private val enterpriseRepository: EnterpriseRepository,
) {
    @Transactional
    fun createMvpTest(
        enterpriseId: Long,
        request: CreateMvpTestRequest,
        mainImageFile: MultipartFile
    ): MvpTestResponse {
        val enterprise = enterpriseRepository.findByIdOrNull(enterpriseId)
            ?: throw ModelNotFoundException("enterprise", enterpriseId)

        if (enterprise.state == EnterpriseState.BLOCKED) throw NoPermissionException(MvpTestErrorMessage.BLOCKED_STATE_ENTERPRISE.message)
        checkRequirement(
            recruitStartDate = request.recruitStartDate,
            recruitEndDate = request.recruitEndDate,
            testStartDate = request.testStartDate,
            testEndDate = request.testEndDate,
            minAge = request.requirementMinAge,
            maxAge = request.requirementMaxAge
        )
        if (mainImageFile.isEmpty) throw IllegalArgumentException(MvpTestErrorMessage.MAIN_URL_NOT_EXIST.message)

        val file = s3Service.uploadMvpTestFile(mainImageFile)

        val mvpTest = request.toMvpTest(enterpriseId, file)
            .let { mvpTestRepository.save(it) }
        request.categories.forEach {
            val category = categoryRepository.findByName(it)
                ?: throw IllegalArgumentException(CategoryErrorMessage.NOT_EXIST.message)
            MvpTestCategoryMap(
                mvpTest = mvpTest,
                category = category
            ).let { map -> mvpTestCategoryMapRepository.save(map) }
        }
        return MvpTestResponse.from(mvpTest, enterprise, request.categories)
    }

    @Transactional
    fun updateMvpTest(
        enterpriseId: Long,
        testId: Long,
        request: UpdateMvpTestRequest,
        mainImageFile: MultipartFile
    ): MvpTestResponse {
        checkRequirement(
            recruitStartDate = request.recruitStartDate,
            recruitEndDate = request.recruitEndDate,
            testStartDate = request.testStartDate,
            testEndDate = request.testEndDate,
            minAge = request.requirementMinAge,
            maxAge = request.requirementMaxAge
        )
        val mvpTest = mvpTestRepository.findByIdOrNull(testId)
            ?: throw ModelNotFoundException("MvpTest", testId)
        if (mvpTest.enterpriseId != enterpriseId) throw NoPermissionException(MvpTestErrorMessage.NOT_AUTHORIZED.message)
        val enterprise = enterpriseRepository.findByIdOrNull(mvpTest.enterpriseId) ?: throw ModelNotFoundException(
            "enterprise",
            mvpTest.enterpriseId
        )
        mvpTestCategoryMapRepository.findAllByMvpTestId(testId)
            .let { mvpTestCategoryMapRepository.deleteAll(it) }

        request.categories.forEach {
            val category = categoryRepository.findByName(it)
                ?: throw IllegalArgumentException(CategoryErrorMessage.NOT_EXIST.message)
            MvpTestCategoryMap(
                mvpTest = mvpTest,
                category = category
            ).let { map -> mvpTestCategoryMapRepository.save(map) }
        }
        if (mainImageFile.isEmpty) throw IllegalArgumentException(MvpTestErrorMessage.MAIN_URL_NOT_EXIST.message)

        mvpTest.mainImageUrl.let { s3Service.deleteFile(it) }
        val file = mainImageFile.let { s3Service.uploadMvpTestFile(it) }

        mvpTest.update(request.toObject(file))
        return MvpTestResponse.from(mvpTest, enterprise, request.categories)
    }

    @Transactional
    @CacheEvict(value = ["mvpTest"], key = "#testId", cacheManager = "redisCacheManager")
    fun deleteMvpTest(enterpriseId: Long, testId: Long) {
        val mvpTest = mvpTestRepository.findByIdOrNull(testId)
            ?: throw ModelNotFoundException("MvpTest", testId)
        if (mvpTest.enterpriseId != enterpriseId) throw NoPermissionException(MvpTestErrorMessage.NOT_AUTHORIZED.message)
        mvpTest.mainImageUrl.let { s3Service.deleteFile(it) }
        mvpTestRepository.delete(mvpTest)
    }

    @Cacheable(value = ["mvpTest"], key = "#testId", cacheManager = "redisCacheManager")
    fun getMvpTest(testId: Long): MvpTestResponse {
        val mvpTest = mvpTestRepository.findByIdOrNull(testId)
            ?: throw ModelNotFoundException("MvpTest", testId)
        val enterprise = enterpriseRepository.findByIdOrNull(mvpTest.enterpriseId) ?: throw ModelNotFoundException(
            "enterprise",
            mvpTest.enterpriseId
        )
        val categories = mvpTestCategoryMapRepository.findAllByMvpTestId(testId)
            .map { it.category.name }
        return MvpTestResponse.from(mvpTest, enterprise, categories)
    }

    fun getMvpTestListSorted(
        sortBy: MvpTestSortType,
        cursorDate: LocalDateTime?,
        cursorId: Long?,
        size: Int
    ): DateCursorPageResponse<MvpTestResponse> {
        val items = mvpTestRepository.findMvpTestListByDateCursor(sortBy, cursorDate, cursorId, size)
            .map { mvpTest ->
                val enterprise = enterpriseRepository.findByIdOrNull(mvpTest.enterpriseId)!!
                val categories = mvpTestCategoryMapRepository.findAllByMvpTestId(mvpTest.id!!)
                    .map { it.category.name }
                MvpTestResponse.from(mvpTest, enterprise, categories)
            }
        val dateExtractor: (MvpTestResponse) -> LocalDateTime = when (sortBy) {
            MvpTestSortType.TEST_START_DATE -> { it -> it.testStartDate }
            MvpTestSortType.TEST_END_DATE -> { it -> it.testEndDate }
        }
        return DateCursorPageResponse.of(items, size, dateExtractor) { it.id }
    }

    fun getMvpTestList(cursor: Long?, size: Int): CursorPageResponse<MvpTestResponse> {
        val items = mvpTestRepository.findMvpTestListByCursor(cursor, size)
            .map { mvpTest ->
                val enterprise = enterpriseRepository.findByIdOrNull(mvpTest.enterpriseId)!!
                val categories = mvpTestCategoryMapRepository.findAllByMvpTestId(mvpTest.id!!)
                    .map { it.category.name }
                MvpTestResponse.from(mvpTest, enterprise, categories)
            }
        return CursorPageResponse.of(items, size) { it.id }
    }

    @Transactional
    fun applyToMvpTest(memberId: Long, testId: Long) {
        val test = mvpTestRepository.findByIdOrNull(testId) ?: throw ModelNotFoundException("mvpTest", testId)
        val member = memberRepository.findByIdOrNull(memberId) ?: throw ModelNotFoundException("member", memberId)
        memberService.checkMemberActive(member)
        checkMemberCondition(member, test)
        require(
            !memberTestRepository.existsByMemberIdAndTestId(
                memberId,
                testId
            )
        ) { MvpTestErrorMessage.ALREADY_APPLIED }
        if (test.recruitType == RecruitType.FIRST_COME) {
            val lock = redissonService.getLock("applyToMvpTest:$testId", 2000, 6000)
            val recruitCount = memberTestRepository.countByTestIdAndState(testId, MemberTestState.APPROVED)
            check(recruitCount < test.recruitNum) { MvpTestErrorMessage.TEST_ALREADY_FULL.message }
            MemberTest(
                member = member,
                test = test,
                state = MemberTestState.APPROVED
            ).let { memberTestRepository.save(it) }
            redissonService.unlock(lock)
        } else {
            MemberTest(
                member = member,
                test = test,
                state = MemberTestState.PENDING
            ).let { memberTestRepository.save(it) }
        }
    }

    @Transactional
    fun getMvpTestListByMember(memberId: Long): List<MvpTestListResponse> {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw ModelNotFoundException("member", memberId)
        return memberTestRepository.findAllByMemberId(memberId).map {
            MvpTestListResponse.from(it)
        }
    }

    fun getMemberList(testId: Long, enterpriseId: Long): List<MemberInfoResponse> {
        return mvpTestRepository.findMemberList(testId, enterpriseId)
    }

    fun getMvpTestsByEnterprise(enterpriseId: Long): List<MvpTestResponse> {
        val test = mvpTestRepository.findAllByEnterpriseId(enterpriseId)
        val enterprise = enterpriseRepository.findByIdOrNull(enterpriseId) ?: throw ModelNotFoundException(
            "enterprise",
            enterpriseId
        )
        return test.map { mvpTest ->
            val categories = mvpTestCategoryMapRepository.findAllByMvpTestId(mvpTest.id!!).map { it.category.name }
            MvpTestResponse.from(mvpTest, enterprise, categories)
        }
    }

    fun getAvailableTests(memberId: Long, pageable: Pageable): List<MvpTestResponse> {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw ModelNotFoundException("Member", memberId)
        checkMemberActive(member)
        val availableTests = mvpTestRepository.findAvailableTests(member, pageable)
        return availableTests.map { mvpTest ->
            val categories = mvpTestCategoryMapRepository.findAllByMvpTestId(mvpTest.id!!).map { it.category.name }
            val enterprise = enterpriseRepository.findByIdOrNull(memberId)
            MvpTestResponse.from(mvpTest, enterprise!!, categories)
        }
    }


    private fun checkRequirement(
        recruitStartDate: LocalDateTime,
        recruitEndDate: LocalDateTime,
        testStartDate: LocalDateTime,
        testEndDate: LocalDateTime,
        minAge: Int?,
        maxAge: Int?
    ) {
        require(
            isRecruitDateValid(
                recruitStartDate,
                recruitEndDate
            )
        ) { MvpTestErrorMessage.RECRUIT_DATE_INVALID.message }
        require(isTestDateValid(recruitEndDate, testStartDate, testEndDate)) {
            MvpTestErrorMessage.TEST_DATE_INVALID.message
        }
        require(
            isAgeRuleValid(
                minAge,
                maxAge
            )
        ) { MvpTestErrorMessage.AGE_RULE_INVALID.message }
    }

    private fun isRecruitDateValid(startDate: LocalDateTime, endDate: LocalDateTime): Boolean {
        return startDate.isBefore(endDate)
    }

    private fun isTestDateValid(
        recruitEndDate: LocalDateTime,
        testStartDate: LocalDateTime,
        testEndDate: LocalDateTime
    ): Boolean {
        return testStartDate.isAfter(recruitEndDate) &&
                testEndDate.isAfter(testStartDate)
    }

    private fun isAgeRuleValid(minAge: Int?, maxAge: Int?): Boolean {
        return if (maxAge == null || minAge == null) true
        else if (minAge <= maxAge) true
        else false
    }

    private fun checkMemberCondition(member: Member, test: MvpTest) {
        if (member.state != MemberState.ACTIVE) {
            throw IllegalStateException(MvpTestErrorMessage.NOT_ACTIVE_USER.message)
        }
        if (test.requirementSex != null && test.requirementSex != member.sex) {
            throw IllegalStateException(MvpTestErrorMessage.SEX_RULE_INVALID.message)
        }
        if (test.requirementMinAge != null && test.requirementMinAge!! > member.age!!) {
            throw IllegalStateException(MvpTestErrorMessage.AGE_RULE_INVALID.message)
        }
        if (test.requirementMaxAge != null && test.requirementMaxAge!! < member.age!!) {
            throw IllegalStateException(MvpTestErrorMessage.AGE_RULE_INVALID.message)
        }
    }

    private fun checkMemberActive(member: Member) {
        if (member.state != MemberState.ACTIVE) throw NoPermissionException(MemberErrorMessage.NOT_ACTIVE.message)
    }

}