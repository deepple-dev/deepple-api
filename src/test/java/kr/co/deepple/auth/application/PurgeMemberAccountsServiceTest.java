package kr.co.deepple.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PurgeMemberAccountsServiceTest {

  @Mock private MemberAccountRepository memberAccountRepository;

  @InjectMocks private PurgeMemberAccountsService purgeMemberAccountsService;

  @Test
  @DisplayName("탈퇴 후 3개월 하루가 지난 계정을 지운다.")
  void purgeAccountsDeletedMoreThanThreeMonthsAndOneDayAgo() {
    LocalDateTime before = LocalDateTime.now().minusMonths(3).minusDays(1);

    purgeMemberAccountsService.purge();

    LocalDateTime after = LocalDateTime.now().minusMonths(3).minusDays(1);
    ArgumentCaptor<LocalDateTime> threshold = ArgumentCaptor.forClass(LocalDateTime.class);
    verify(memberAccountRepository).deleteAllDeletedBefore(threshold.capture());
    assertThat(threshold.getValue()).isBetween(before, after);
  }
}
