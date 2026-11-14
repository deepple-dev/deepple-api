package kr.co.deepple.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.in.MemberSummary;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindMemberSummaryServiceTest {

  @Mock private MemberRepository memberRepository;

  @InjectMocks private FindMemberSummaryService findMemberSummaryService;

  @Test
  @DisplayName("회원의 요약 정보를 문자열과 기본 타입으로 돌려준다.")
  void findSummaryOfMember() {
    Member member =
        MemberFixture.builder().id(1L).nickname("딥플이").gender(Gender.FEMALE).vip(true).build();
    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

    MemberSummary summary = findMemberSummaryService.getMemberSummary(1L);

    assertThat(summary.memberId()).isEqualTo(1L);
    assertThat(summary.nickname()).isEqualTo("딥플이");
    assertThat(summary.gender()).isEqualTo("FEMALE");
    assertThat(summary.activityStatus()).isEqualTo("ACTIVE");
    assertThat(summary.grade()).isEqualTo(member.getGrade().name());
    assertThat(summary.vip()).isTrue();
    assertThat(summary.isActive()).isTrue();
  }

  @Test
  @DisplayName("반대 성별과 연애고사 제출 여부를 돌려준다.")
  void findOppositeGenderAndDatingExamSubmission() {
    Member female =
        MemberFixture.builder().id(1L).gender(Gender.FEMALE).datingExamSubmitted(true).build();
    Member male =
        MemberFixture.builder().id(2L).gender(Gender.MALE).datingExamSubmitted(false).build();
    when(memberRepository.findById(1L)).thenReturn(Optional.of(female));
    when(memberRepository.findById(2L)).thenReturn(Optional.of(male));

    MemberSummary femaleSummary = findMemberSummaryService.getMemberSummary(1L);
    MemberSummary maleSummary = findMemberSummaryService.getMemberSummary(2L);

    assertThat(femaleSummary.oppositeGender()).isEqualTo("MALE");
    assertThat(femaleSummary.datingExamSubmitted()).isTrue();
    assertThat(maleSummary.oppositeGender()).isEqualTo("FEMALE");
    assertThat(maleSummary.datingExamSubmitted()).isFalse();
  }

  @Test
  @DisplayName("프로필을 작성하지 않은 회원은 프로필에서 오는 값이 비어 있다.")
  void findSummaryOfMemberWithoutProfile() {
    Member member =
        MemberFixture.builder().id(1L).activityStatus(ActivityStatus.INITIAL).noProfile().build();
    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

    MemberSummary summary = findMemberSummaryService.getMemberSummary(1L);

    assertThat(summary.nickname()).isNull();
    assertThat(summary.gender()).isNull();
    assertThat(summary.age()).isNull();
    assertThat(summary.city()).isNull();
    assertThat(summary.isActive()).isFalse();
    assertThatThrownBy(summary::requireNickname).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(summary::requireGender).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(summary::oppositeGender).isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("없는 회원을 꼭 찾아야 하는 조회는 회원을 찾을 수 없다는 예외를 던진다.")
  void throwWhenMemberNotFound() {
    when(memberRepository.findById(1L)).thenReturn(Optional.empty());

    assertThat(findMemberSummaryService.findMemberSummary(1L)).isEmpty();
    assertThatThrownBy(() -> findMemberSummaryService.getMemberSummary(1L))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("여러 회원을 한 번에 조회하면 회원 번호로 찾을 수 있게 돌려준다.")
  void findSummariesByIds() {
    Member first = MemberFixture.builder().id(1L).build();
    Member second = MemberFixture.builder().id(2L).build();
    when(memberRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(first, second));

    Map<Long, MemberSummary> summaries =
        findMemberSummaryService.findMemberSummaries(List.of(1L, 2L, 2L));

    assertThat(summaries).containsOnlyKeys(1L, 2L);
  }
}
