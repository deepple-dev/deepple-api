package kr.co.deepple.member.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.out.MemberQueryRepository;
import kr.co.deepple.member.domain.MemberErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberQueryServiceTest {

  @Mock private MemberQueryRepository memberQueryRepository;

  @InjectMocks private MemberQueryService memberQueryService;

  @Nested
  @DisplayName("getProfile 메서드 테스트.")
  class getProfileTest {

    @DisplayName("해당 결과가 존재하지 않을 경우, 예외 발생.")
    @Test
    void throwsExceptionWhenResultIsEmpty() {
      // Given
      Long memberId = 1L;
      when(memberQueryRepository.findProfileByMemberId(memberId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> memberQueryService.getProfile(memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }
  }

  @DisplayName("getContact 메서드 테스트")
  @Nested
  class getContactTest {

    @DisplayName("해당 결과가 존재하지 않을 경우, 예외 발생.")
    @Test
    void throwsExceptionWhenResultIsEmpty() {
      // Given
      Long memberId = 1L;
      when(memberQueryRepository.findContactsByMemberId(memberId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> memberQueryService.getContact(memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }
  }
}
