package kr.co.deepple.community.command.domain.profileexchange;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.community.command.domain.CommunityErrorCode;
import kr.co.deepple.community.fixture.ProfileExchangeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProfileExchangeRepositoryTest extends RepositoryTestSupport {

  @Autowired private ProfileExchangeRepository profileExchangeRepository;

  @Test
  @DisplayName("두 회원 사이에 프로필 교환이 있으면 상태·방향과 상관없이 또 저장할 수 없다")
  void rejectSecondProfileExchange() {
    // given
    long memberId = 1L;
    long anotherMemberId = 2L;
    profileExchangeRepository.save(
        ProfileExchangeFixture.builder()
            .requesterId(memberId)
            .responderId(anotherMemberId)
            .status(ProfileExchangeStatus.REJECTED)
            .build());

    // when & then
    assertThatThrownBy(() ->
            profileExchangeRepository.save(
                ProfileExchangeFixture.builder()
                    .requesterId(anotherMemberId)
                    .responderId(memberId)
                    .build()))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue(
            "errorCode", CommunityErrorCode.PROFILE_EXCHANGE_ALREADY_EXISTS);
  }
}
