package kr.co.deepple.member.command.application.member;

import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.auth.application.RefreshTokenService;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.fixture.MemberFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberAuthServiceTest {

  @Mock private MemberCommandRepository memberCommandRepository;

  @Mock private RefreshTokenService refreshTokenService;

  @InjectMocks private MemberAuthService memberAuthService;

  @Nested
  @DisplayName("회원 탈퇴 테스트.")
  class Delete {

    @DisplayName("회원으로 존재하지 않는 경우, 예외 발생.")
    @Test
    void throwExceptionWhenMemberIsNotFound() {
      // Given
      long memberId = 1L;
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

      // When & Then
      Assertions.assertThatThrownBy(() -> memberAuthService.delete(memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @DisplayName("존재하는 경우, 회원을 SoftDelete 하고 모든 리프레시 토큰을 폐기한다.")
    @Test
    void softDeleteWhenMemberIsFound() {
      // Given
      long memberId = 1L;
      Member member = MemberFixture.builder().id(memberId).build();
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberAuthService.delete(memberId);

      // Then
      Assertions.assertThat(member.isDeleted()).isTrue();
      Mockito.verify(refreshTokenService).revokeAll(TokenOwner.member(memberId));
    }
  }
}
