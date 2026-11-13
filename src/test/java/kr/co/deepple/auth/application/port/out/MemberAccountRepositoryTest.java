package kr.co.deepple.auth.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.fixture.MemberAccountFixture;
import kr.co.deepple.common.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class MemberAccountRepositoryTest extends RepositoryTestSupport {

  @Autowired private MemberAccountRepository memberAccountRepository;

  @Autowired private TestEntityManager entityManager;

  @Test
  @DisplayName("기준 시각보다 먼저 탈퇴한 계정만 지운다.")
  void deleteOnlyAccountsDeletedBeforeThreshold() {
    LocalDateTime threshold = LocalDateTime.now().minusMonths(3);
    MemberAccount old =
        entityManager.persist(
            MemberAccountFixture.builder().deletedAt(threshold.minusDays(1)).build());
    MemberAccount recent =
        entityManager.persist(
            MemberAccountFixture.builder().deletedAt(threshold.plusDays(1)).build());
    MemberAccount active = entityManager.persist(MemberAccountFixture.builder().build());
    entityManager.flush();

    memberAccountRepository.deleteAllDeletedBefore(threshold);
    entityManager.clear();

    assertThat(memberAccountRepository.findById(old.getId())).isEmpty();
    assertThat(memberAccountRepository.findById(recent.getId())).isPresent();
    assertThat(memberAccountRepository.findById(active.getId())).isPresent();
  }
}
