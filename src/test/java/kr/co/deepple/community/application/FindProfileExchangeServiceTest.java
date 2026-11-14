package kr.co.deepple.community.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import kr.co.deepple.community.application.port.in.ProfileExchangeSummary;
import kr.co.deepple.community.application.port.out.ProfileExchangeRepository;
import kr.co.deepple.community.domain.ProfileExchangeStatus;
import kr.co.deepple.community.fixture.ProfileExchangeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindProfileExchangeServiceTest {

  @Mock private ProfileExchangeRepository profileExchangeRepository;

  @InjectMocks private FindProfileExchangeService findProfileExchangeService;

  @Test
  @DisplayName("두 회원 사이의 프로필 교환을 문자열 값으로 바꿔 돌려준다.")
  void findProfileExchangeBetween() {
    given(profileExchangeRepository.findProfileExchangeBetween(2L, 1L))
        .willReturn(
            Optional.of(
                ProfileExchangeFixture.builder()
                    .id(10L)
                    .requesterId(1L)
                    .responderId(2L)
                    .status(ProfileExchangeStatus.APPROVE)
                    .build()));

    assertThat(findProfileExchangeService.findProfileExchangeBetween(2L, 1L))
        .contains(new ProfileExchangeSummary(10L, 1L, 2L, "APPROVE"));
  }

  @Test
  @DisplayName("두 회원 사이에 프로필 교환이 없으면 빈 값을 돌려준다.")
  void returnEmptyWhenNoProfileExchange() {
    given(profileExchangeRepository.findProfileExchangeBetween(1L, 2L))
        .willReturn(Optional.empty());

    assertThat(findProfileExchangeService.findProfileExchangeBetween(1L, 2L)).isEmpty();
  }
}
