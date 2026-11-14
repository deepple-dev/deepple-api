package kr.co.deepple.auth.application;

import java.time.LocalDateTime;
import kr.co.deepple.auth.application.port.in.PurgeMemberAccountsUseCase;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurgeMemberAccountsService implements PurgeMemberAccountsUseCase {

  private static final int RETENTION_MONTHS = 3;
  private static final int PURGE_DELAY_DAYS = 1;

  private final MemberAccountRepository memberAccountRepository;

  @Override
  @Transactional
  public void purge() {
    int deletedCount =
        memberAccountRepository.deleteAllDeletedBefore(
            LocalDateTime.now().minusMonths(RETENTION_MONTHS).minusDays(PURGE_DELAY_DAYS));
    log.atInfo().addKeyValue("deletedCount", deletedCount).log("탈퇴 계정 삭제");
  }
}
