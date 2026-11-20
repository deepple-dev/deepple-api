package kr.co.deepple.block.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import kr.co.deepple.block.fixture.BlockFixture;
import kr.co.deepple.common.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class BlockRepositoryTest extends RepositoryTestSupport {

  @Autowired private BlockRepository blockRepository;

  @Autowired private TestEntityManager entityManager;

  @Test
  @DisplayName("내가 차단한 회원 번호만 조회한다.")
  void findBlockedIdsOfBlocker() {
    entityManager.persist(BlockFixture.builder().blockerId(1L).blockedId(2L).build());
    entityManager.persist(BlockFixture.builder().blockerId(1L).blockedId(3L).build());
    entityManager.persist(BlockFixture.builder().blockerId(4L).blockedId(1L).build());
    entityManager.flush();

    assertThat(blockRepository.findBlockedIdsByBlockerId(1L)).containsExactlyInAnyOrder(2L, 3L);
  }

  @Test
  @DisplayName("같은 차단을 두 번 넣어도 한 행만 남는다.")
  void insertIfAbsentOnce() {
    blockRepository.insertIfAbsent(1L, 2L, LocalDateTime.now());
    blockRepository.insertIfAbsent(1L, 2L, LocalDateTime.now());

    assertThat(blockRepository.count()).isEqualTo(1L);
    assertThat(blockRepository.existsByBlockerIdAndBlockedId(1L, 2L)).isTrue();
  }
}
