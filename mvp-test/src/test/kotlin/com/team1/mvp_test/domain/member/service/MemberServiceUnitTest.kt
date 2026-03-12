package com.team1.mvp_test.domain.member.service

import com.team1.mvp_test.common.error.MemberErrorMessage
import com.team1.mvp_test.common.exception.ModelNotFoundException
import com.team1.mvp_test.common.exception.NoPermissionException
import com.team1.mvp_test.domain.member.dto.MemberUpdateRequest
import com.team1.mvp_test.domain.member.dto.SignUpInfoRequest
import com.team1.mvp_test.domain.member.model.Member
import com.team1.mvp_test.domain.member.model.MemberState
import com.team1.mvp_test.domain.member.model.Sex
import com.team1.mvp_test.domain.member.repository.MemberRepository
import com.team1.mvp_test.domain.oauth.client.OAuthLoginUserInfo
import com.team1.mvp_test.domain.oauth.provider.OAuthProvider
import com.team1.mvp_test.infra.redis.RedisService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull

class MemberServiceUnitTest: BehaviorSpec({
    val memberRepository = mockk<MemberRepository>()
    val redisService = mockk<RedisService>()
    val memberService = MemberService(memberRepository, redisService)

    lateinit var activeMember: Member
    lateinit var pendingMember: Member
    beforeTest {
        activeMember = createActiveMember()
        pendingMember = createPendingMember()
    }

    Given("member state 가 active 라면") {
        every { memberRepository.findByIdOrNull(1L)} returns activeMember

        When("updateSignUpInfo 실행 시") {
            Then("IllegalStateException") {
                val exception = shouldThrow<IllegalStateException> {
                    memberService.updateSignUpInfo(1L, signUpInfoRequest)
                }
                exception.message shouldBe MemberErrorMessage.SIGN_UP_ALREADY_DONE.message
            }
        }
    }

    Given("핸드폰 인증번호에 실패했다면") {
        every { memberRepository.findByIdOrNull(any()) } returns pendingMember
        every { redisService.getVerifiedPhoneNumber(any()) } returns null
        When("updateSignUpInfo 실행 시") {
            Then("IllegalStateException") {
                val exception = shouldThrow<IllegalStateException> {
                    memberService.updateSignUpInfo(2L, signUpInfoRequest)
                }
                exception.message shouldBe MemberErrorMessage.PHONE_NUMBER_VERIFY_FAILED.message
            }
        }
    }

    Given("이미 가입되어있는 핸드폰 번호라면") {
        every { memberRepository.findByIdOrNull(any()) } returns pendingMember
        every { redisService.getVerifiedPhoneNumber(any()) } returns "01234567890"
        every { memberRepository.existsByPhoneNumber(any()) } returns true
        When("updateSignUpInfo 실행 시") {
            Then("IllegalStateException") {
                val exception = shouldThrow<IllegalStateException> {
                    memberService.updateSignUpInfo(2L, AlreadySignUpInfoRequest)
                }
                exception.message shouldBe MemberErrorMessage.PHONE_NUMBER_ALREADY_IN_USE.message
            }
        }
    }

    Given("핸드폰 인증까지 성공했다면") {
        every { memberRepository.findByIdOrNull(2L) } returns pendingMember
        every { redisService.getVerifiedPhoneNumber(any()) } returns "00123456789"
        every { memberRepository.existsByPhoneNumber(any()) } returns false
        When("updateSignUpInfo 실행 시") {
            Then("유저 상세정보가 저장되고 반환된다") {
                val memberInfo = memberService.updateSignUpInfo(2L, signUpInfoRequest)
                memberInfo.id shouldBe 2L
                memberInfo.age shouldBe signUpInfoRequest.age
                memberInfo.sex shouldBe signUpInfoRequest.sex.toString()
                memberInfo.name shouldBe signUpInfoRequest.name
            }
        }
    }

    Given("member state가 active 가 아니라면") {
        every { memberRepository.findByIdOrNull(2L)} returns pendingMember
        When("updateMember 실행 시") {
            Then("NoPermissionException") {
                val exception = shouldThrow<NoPermissionException> {
                    memberService.updateMember(2L, memberUpdateRequest)
                }
                exception.message shouldBe MemberErrorMessage.NOT_ACTIVE.message
            }
        }
    }

    Given("member state가 active 인 상태라면") {
        every { memberRepository.findByIdOrNull(1L)} returns activeMember
        When("updateMember 실행 시") {
            Then("member 정보가 바뀌고 반환된다") {
                val memberInfo = memberService.updateMember(1L, memberUpdateRequest)
                memberInfo.info shouldBe  memberUpdateRequest.info
                memberInfo.name shouldBe  memberUpdateRequest.name
            }
        }
    }

    Given("존재하지 않는 member id 라면") {
        every { memberRepository.findByIdOrNull(any())} returns null
        When("getMemberById 실행 시") {
            Then("ModelNotFoundException") {
                shouldThrow<ModelNotFoundException> {
                    memberService.getMemberById(2L)
                }
            }
        }
    }

    Given("존재하는 member id 라면") {
        every { memberRepository.findByIdOrNull(any())} returns activeMember
        When("getMemberById 실행 시") {
            Then("해당 id의 member 정보가 반환된다") {
                val member = memberService.getMemberById(1L)
                member.id shouldBe activeMember.id
                member.sex shouldBe activeMember.sex.toString()
                member.age shouldBe activeMember.age
                member.info shouldBe activeMember.info
                member.email shouldBe activeMember.email
                member.name shouldBe activeMember.name
            }
        }
    }

    Given("가입되지 않은 요청이라면") {
        every { memberRepository.findByProviderIdAndProviderName(oAuthLoginUserInfo.id, oAuthLoginUserInfo.provider) } returns null
        every { memberRepository.save(any()) } returns Member(id = 3L, email = oAuthLoginUserInfo.email, providerName = oAuthLoginUserInfo.provider, providerId = oAuthLoginUserInfo.id)
        When("registerIfAbsent 실행 시") {
            Then("해당 요청으로 가입된다") {
                val member = memberService.registerIfAbsent(oAuthLoginUserInfo)
                member.email shouldBe oAuthLoginUserInfo.email
                member.providerName shouldBe oAuthLoginUserInfo.provider
                member.providerId shouldBe oAuthLoginUserInfo.id
            }
        }
    }

    Given("이미 가입된 요청이라면") {
        every { memberRepository.findByProviderIdAndProviderName(oAuthLoginUserInfo.id, oAuthLoginUserInfo.provider) } returns activeMember
        When("registerIfAbsent 실행 시") {
            Then("해당 id로 로그인이 요청된다") {
                val member = memberService.registerIfAbsent(oAuthLoginUserInfo)
                member.id shouldBe activeMember.id
                member.email shouldBe activeMember.email
                member.providerName shouldBe activeMember.providerName
                member.providerId shouldBe activeMember.providerId
                member.state shouldBe activeMember.state
            }
        }
    }


}) {
    companion object {
        fun createActiveMember() = Member(
            id = 1L,
            name = "test",
            email = "test@test.com",
            age = 30,
            sex = Sex.MALE,
            phoneNumber = "01234567890",
            providerName = OAuthProvider.GOOGLE,
            providerId = "test provider id",
            state = MemberState.ACTIVE
        )

        fun createPendingMember() = Member(
            id = 2L,
            name = "test2",
            email = "test2@test.com",
            state = MemberState.PENDING
        )
        private val AlreadySignUpInfoRequest = SignUpInfoRequest(
            age = 30,
            info = "test info",
            name = "test name",
            phoneNumber = "01234567890",
            sex = Sex.MALE
        )
        private val signUpInfoRequest = SignUpInfoRequest(
            age = 30,
            info = "test info",
            name = "test name",
            phoneNumber = "00123456789",
            sex = Sex.MALE
        )
        private val memberUpdateRequest = MemberUpdateRequest(
            info = "test info 2",
            name = "test name 2"
        )
        private val oAuthLoginUserInfo = OAuthLoginUserInfo(
            email = "test@test.com",
            id = "test provider id",
            provider = OAuthProvider.GOOGLE
        )
    }
}