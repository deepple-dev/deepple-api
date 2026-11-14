package kr.co.deepple.datingexam.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.port.out.PersonalityTypeMemberQueryRepository;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.datingexam.domain.DatingExamErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonalityTypeMemberQueryServiceTest {

  private static final long MEMBER_ID = 1L;

  @Mock private PersonalityTypeMemberQueryRepository personalityTypeMemberQueryRepository;

  @Mock private IntroducibleMemberFilter introducibleMemberFilter;

  @InjectMocks private PersonalityTypeMemberQueryService personalityTypeMemberQueryService;

  @Test
  @DisplayName("문자열로 받은 유형의 회원을 찾아 소개할 수 있는 회원만 돌려준다.")
  void findMemberIdsOfType() {
    when(personalityTypeMemberQueryRepository.findMemberIdsByDominantType(
            AnswerPersonalityType.RATIONAL_REALIST))
        .thenReturn(Set.of(4L, 3L, 2L));
    when(introducibleMemberFilter.filter(MEMBER_ID, Set.of(4L, 3L, 2L)))
        .thenReturn(List.of(4L, 2L));

    List<Long> memberIds =
        personalityTypeMemberQueryService.findMemberIdsByDominantType(
            MEMBER_ID, "RATIONAL_REALIST");

    assertThat(memberIds).containsExactly(4L, 2L);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "UNKNOWN", "rational_realist"})
  @DisplayName("유형 값이 잘못되면 회원을 찾지 않고 예외를 던진다.")
  void throwWhenTypeIsInvalid(String personalityType) {
    assertThatThrownBy(() ->
            personalityTypeMemberQueryService.findMemberIdsByDominantType(
                MEMBER_ID, personalityType))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", DatingExamErrorCode.INVALID_PERSONALITY_TYPE);
    verifyNoInteractions(personalityTypeMemberQueryRepository, introducibleMemberFilter);
  }

  @ParameterizedTest
  @EnumSource(AnswerPersonalityType.class)
  @DisplayName("유형 이름과 같은 값은 검증을 통과한다.")
  void validateType(AnswerPersonalityType type) {
    assertThatCode(() -> personalityTypeMemberQueryService.validate(type.name()))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "UNKNOWN", "rational_realist"})
  @DisplayName("유형 이름과 다른 값은 검증에서 예외를 던진다.")
  void throwWhenValidatedTypeIsInvalid(String personalityType) {
    assertThatThrownBy(() -> personalityTypeMemberQueryService.validate(personalityType))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", DatingExamErrorCode.INVALID_PERSONALITY_TYPE);
  }
}
