package kr.co.deepple.datingexam.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.block.application.port.in.FindBlockedMemberIdsUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.domain.DatingExamErrorCode;
import kr.co.deepple.member.application.port.in.FilterIntroducibleMemberIdsUseCase;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.application.port.in.MemberSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IntroducibleMemberFilterTest {

  private static final long MEMBER_ID = 1L;

  @Mock private FindBlockedMemberIdsUseCase findBlockedMemberIdsUseCase;

  @Mock private FindMemberSummaryUseCase findMemberSummaryUseCase;

  @Mock private FilterIntroducibleMemberIdsUseCase filterIntroducibleMemberIdsUseCase;

  @InjectMocks private IntroducibleMemberFilter introducibleMemberFilter;

  @Test
  @DisplayName("차단 관계인 회원과 본인을 뺀 후보만 소개 가능 여부를 확인한다.")
  void excludeBlockRelatedMembersAndSelf() {
    when(findBlockedMemberIdsUseCase.findBlockRelatedMemberIds(MEMBER_ID)).thenReturn(Set.of(3L));
    when(findMemberSummaryUseCase.findMemberSummary(MEMBER_ID))
        .thenReturn(Optional.of(summary("MALE")));
    when(filterIntroducibleMemberIdsUseCase.filterIntroducibleMemberIds(MEMBER_ID, Set.of(2L, 4L)))
        .thenReturn(List.of(4L, 2L));

    List<Long> result = introducibleMemberFilter.filter(MEMBER_ID, Set.of(MEMBER_ID, 2L, 3L, 4L));

    assertThat(result).containsExactly(4L, 2L);
  }

  @Test
  @DisplayName("차단 관계인 회원과 본인을 빼고 남는 후보가 없으면 성별을 확인하지 않고 빈 목록을 돌려준다.")
  void returnEmptyWithoutCheckingGenderWhenNoCandidateRemains() {
    when(findBlockedMemberIdsUseCase.findBlockRelatedMemberIds(MEMBER_ID)).thenReturn(Set.of(3L));

    List<Long> result = introducibleMemberFilter.filter(MEMBER_ID, Set.of(MEMBER_ID, 3L));

    assertThat(result).isEmpty();
    verifyNoInteractions(findMemberSummaryUseCase, filterIntroducibleMemberIdsUseCase);
  }

  @Test
  @DisplayName("성별이 없는 회원이면 성별이 설정되지 않았다는 예외를 던진다.")
  void throwWhenGenderIsNotSet() {
    when(findBlockedMemberIdsUseCase.findBlockRelatedMemberIds(MEMBER_ID)).thenReturn(Set.of());
    when(findMemberSummaryUseCase.findMemberSummary(MEMBER_ID))
        .thenReturn(Optional.of(summary(null)));

    assertThatThrownBy(() -> introducibleMemberFilter.filter(MEMBER_ID, Set.of(2L)))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", DatingExamErrorCode.GENDER_NOT_SET);
    verifyNoInteractions(filterIntroducibleMemberIdsUseCase);
  }

  @Test
  @DisplayName("회원을 찾을 수 없어도 성별이 설정되지 않았다는 예외를 던진다.")
  void throwWhenMemberIsMissing() {
    when(findBlockedMemberIdsUseCase.findBlockRelatedMemberIds(MEMBER_ID)).thenReturn(Set.of());
    when(findMemberSummaryUseCase.findMemberSummary(MEMBER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> introducibleMemberFilter.filter(MEMBER_ID, Set.of(2L)))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", DatingExamErrorCode.GENDER_NOT_SET);
  }

  private MemberSummary summary(String gender) {
    return new MemberSummary(
        MEMBER_ID, "닉네임", gender, 30, "SEOUL", null, "SILVER", "ACTIVE", false, false);
  }
}
