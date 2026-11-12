package kr.co.deepple.member.command.application.introduction;


import kr.co.deepple.block.application.port.out.BlockRepository;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.port.out.SoulmateQueryRepository;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.introduction.IntroductionType;
import kr.co.deepple.member.command.domain.introduction.MemberIntroduction;
import kr.co.deepple.member.command.domain.introduction.MemberIntroductionCommandRepository;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberIntroductionServiceTest {

    @InjectMocks
    private MemberIntroductionService memberIntroductionService;

    @Mock
    private MemberIntroductionCommandRepository memberIntroductionCommandRepository;

    @Mock
    private MemberCommandRepository memberCommandRepository;

    @Mock
    private SoulmateQueryRepository soulmateQueryRepository;

    @Mock
    private BlockRepository blockRepository;

    @Test
    @DisplayName("소개받은 멤버가 존재하지 않으면 예외를 던진다.")
    void throwsExceptionWhenIntroducedMemberNotFound() {
        // given
        long memberId = 1L;
        long introducedMemberId = 2L;

        when(memberCommandRepository.findById(introducedMemberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberIntroductionService.createGradeIntroduction(memberId, introducedMemberId))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("소개받은 멤버가 Active 상태가 아니라면 예외를 던진다")
    void throwsExceptionWhenIntroducedMemberNotActive() {
        // given
        long memberId = 1L;
        long introducedMemberId = 2L;

        Member introducedMember = MemberFixture.builder()
            .id(introducedMemberId)
            .activityStatus(ActivityStatus.DORMANT)
            .build();
        when(memberCommandRepository.findById(introducedMemberId)).thenReturn(Optional.of(introducedMember));

        // when & then
        assertThatThrownBy(() -> memberIntroductionService.createGradeIntroduction(memberId, introducedMemberId))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INTRODUCED_MEMBER_NOT_ACTIVE);
    }

    @Test
    @DisplayName("소개받은 멤버를 차단한 상태라면 예외를 던진다")
    void throwsExceptionWhenMemberBlocksIntroducedMember() {
        // given
        long memberId = 1L;
        long introducedMemberId = 2L;

        Member introducedMember = MemberFixture.builder().id(introducedMemberId).build();
        when(memberCommandRepository.findById(introducedMemberId)).thenReturn(Optional.of(introducedMember));
        when(blockRepository.existsByBlockerIdAndBlockedId(memberId, introducedMemberId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberIntroductionService.createGradeIntroduction(memberId, introducedMemberId))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INTRODUCED_MEMBER_BLOCKED);
    }

    @Test
    @DisplayName("소개받은 멤버가 차단된 상태라면 예외를 던진다")
    void throwsExceptionWhenIntroducedMemberBlocksMember() {
        // given
        long memberId = 1L;
        long introducedMemberId = 2L;

        Member introducedMember = MemberFixture.builder().id(introducedMemberId).build();
        when(memberCommandRepository.findById(introducedMemberId)).thenReturn(Optional.of(introducedMember));
        when(blockRepository.existsByBlockerIdAndBlockedId(memberId, introducedMemberId)).thenReturn(false);
        when(blockRepository.existsByBlockerIdAndBlockedId(introducedMemberId, memberId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberIntroductionService.createGradeIntroduction(memberId, introducedMemberId))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INTRODUCED_MEMBER_BLOCKED);
    }


    @Test
    @DisplayName("이미 소개받은 멤버라면 예외를 던진다")
    void throwsExceptionWhenMemberAlreadyIntroduced() {
        // given
        long memberId = 1L;
        long introducedMemberId = 2L;

        Member introducedMember = MemberFixture.builder().id(introducedMemberId).build();
        when(memberCommandRepository.findById(introducedMemberId)).thenReturn(Optional.of(introducedMember));
        when(blockRepository.existsByBlockerIdAndBlockedId(memberId, introducedMemberId)).thenReturn(false);
        when(blockRepository.existsByBlockerIdAndBlockedId(introducedMemberId, memberId)).thenReturn(false);
        when(memberIntroductionCommandRepository.existsByMemberIdAndIntroducedMemberId(memberId,
            introducedMemberId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberIntroductionService.createGradeIntroduction(memberId, introducedMemberId))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_INTRODUCTION_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("소개받은 멤버가 아니라면 소개를 생성한다")
    void createIntroduction() {
        // given
        long memberId = 1L;
        long introducedMemberId = 2L;

        Member introducedMember = MemberFixture.builder().id(introducedMemberId).build();
        when(memberCommandRepository.findById(introducedMemberId)).thenReturn(Optional.of(introducedMember));
        when(blockRepository.existsByBlockerIdAndBlockedId(memberId, introducedMemberId)).thenReturn(false);
        when(blockRepository.existsByBlockerIdAndBlockedId(introducedMemberId, memberId)).thenReturn(false);
        when(memberIntroductionCommandRepository.existsByMemberIdAndIntroducedMemberId(memberId,
            introducedMemberId)).thenReturn(false);

        // when
        try (MockedStatic<MemberIntroduction> memberIntroductionMock = mockStatic(MemberIntroduction.class)) {
            MemberIntroduction memberIntroduction = mock(MemberIntroduction.class);
            memberIntroductionMock.when(() -> MemberIntroduction.of(memberId, introducedMemberId,
                    IntroductionType.DIAMOND_GRADE))
                .thenReturn(memberIntroduction);
            memberIntroductionService.createGradeIntroduction(memberId, introducedMemberId);

            // then
            verify(memberIntroductionCommandRepository).save(memberIntroduction);
        }
    }

    @Test
    @DisplayName("소울 메이트 소개 생성에서, 연애고사 미제출 회원은 예외를 던진다")
    void throwsExceptionWhenMemberNotSubmittedDatingExam() {
        // given
        long memberId = 1L;
        long introducedMemberId = 2L;

        Member member = MemberFixture.builder().id(memberId).datingExamSubmitted(false).build();
        when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> memberIntroductionService.createSoulmateIntroduction(memberId, introducedMemberId))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.DATING_EXAM_NOT_SUBMITTED);
    }

    @Test
    @DisplayName("소울 메이트 소개 생성에서, 소울메이트가 아닌 회원은 예외를 던진다")
    void throwsExceptionWhenNotSoulmate() {
        // given
        long memberId = 1L;
        long introducedMemberId = 2L;

        Member member = MemberFixture.builder().id(memberId).datingExamSubmitted(true).build();
        when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(soulmateQueryRepository.findSameAnswerMemberIds(memberId)).thenReturn(
            Set.of(3L, 4L, 5L)
        );

        // when & then
        assertThatThrownBy(() -> memberIntroductionService.createSoulmateIntroduction(memberId, introducedMemberId))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.NOT_SOULMATE);
    }

    @Test
    @DisplayName("같은 답안 소개 생성에서, 같은 답안을 제출한 회원이 아니라면 예외를 던진다")
    void throwsExceptionWhenNotSameAnswerMember() {
        // given
        long memberId = 1L;
        long introducedMemberId = 2L;

        when(soulmateQueryRepository.findSameAnswerMemberIds(memberId)).thenReturn(
            Set.of(3L, 4L, 5L)
        );

        // when & then
        assertThatThrownBy(() -> memberIntroductionService.createSameAnswerIntroduction(memberId, introducedMemberId))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.NOT_SAME_ANSWER_MEMBER);
    }
}
