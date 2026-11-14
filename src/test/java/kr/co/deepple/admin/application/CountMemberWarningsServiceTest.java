package kr.co.deepple.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;
import kr.co.deepple.admin.application.port.out.WarningCommandRepository;
import kr.co.deepple.admin.application.port.out.WarningCommandRepository.MemberWarningCount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CountMemberWarningsServiceTest {

  @Mock private WarningCommandRepository warningCommandRepository;

  @InjectMocks private CountMemberWarningsService countMemberWarningsService;

  @Test
  @DisplayName("경고가 있는 회원의 경고 수를 회원 번호별로 돌려준다.")
  void countMemberWarnings() {
    given(warningCommandRepository.countByMemberIds(List.of(1L, 2L, 3L)))
        .willReturn(List.of(count(1L, 2L), count(2L, 1L)));

    assertThat(countMemberWarningsService.countMemberWarnings(List.of(1L, 2L, 3L)))
        .containsOnly(Map.entry(1L, 2), Map.entry(2L, 1));
  }

  @Test
  @DisplayName("회원 번호가 없으면 조회하지 않고 빈 결과를 돌려준다.")
  void returnEmptyWhenNoMemberIds() {
    assertThat(countMemberWarningsService.countMemberWarnings(List.of())).isEmpty();
    verifyNoInteractions(warningCommandRepository);
  }

  private MemberWarningCount count(long memberId, long warningCount) {
    return new MemberWarningCount() {
      @Override
      public long getMemberId() {
        return memberId;
      }

      @Override
      public long getWarningCount() {
        return warningCount;
      }
    };
  }
}
