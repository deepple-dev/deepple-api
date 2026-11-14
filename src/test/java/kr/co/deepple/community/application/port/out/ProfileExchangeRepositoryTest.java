package kr.co.deepple.community.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.community.domain.ProfileExchange;
import kr.co.deepple.community.domain.ProfileExchangeStatus;
import kr.co.deepple.community.fixture.ProfileExchangeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

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
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @ParameterizedTest
  @EnumSource(ProfileExchangeStatus.class)
  @DisplayName("두 회원 사이의 프로필 교환을 상태·방향과 상관없이 찾는다")
  void findProfileExchangeBetween(ProfileExchangeStatus status) {
    // given
    long requesterId = 1L;
    long responderId = 2L;
    ProfileExchange profileExchange =
        profileExchangeRepository.save(
            ProfileExchangeFixture.builder()
                .requesterId(requesterId)
                .responderId(responderId)
                .status(status)
                .build());
    profileExchangeRepository.save(
        ProfileExchangeFixture.builder().requesterId(requesterId).responderId(3L).build());

    // when & then
    assertThat(profileExchangeRepository.findProfileExchangeBetween(requesterId, responderId))
        .get()
        .extracting(
            ProfileExchange::getId,
            ProfileExchange::getRequesterId,
            ProfileExchange::getResponderId,
            ProfileExchange::getStatus)
        .containsExactly(profileExchange.getId(), requesterId, responderId, status);
    assertThat(profileExchangeRepository.findProfileExchangeBetween(responderId, requesterId))
        .get()
        .extracting(ProfileExchange::getId)
        .isEqualTo(profileExchange.getId());
  }

  @Test
  @DisplayName("두 회원 사이에 프로필 교환이 없으면 빈 값을 돌려준다")
  void returnEmptyWhenNoProfileExchangeBetween() {
    // given
    profileExchangeRepository.save(
        ProfileExchangeFixture.builder().requesterId(1L).responderId(3L).build());

    // when & then
    assertThat(profileExchangeRepository.findProfileExchangeBetween(1L, 2L)).isEmpty();
  }
}
