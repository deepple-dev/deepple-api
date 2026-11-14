package kr.co.deepple.like.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.like.domain.Like;
import kr.co.deepple.like.domain.LikeLevel;
import kr.co.deepple.like.fixture.LikeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class LikeCommandRepositoryTest extends RepositoryTestSupport {

  @Autowired private LikeCommandRepository likeCommandRepository;

  @Autowired private TestEntityManager em;

  @Test
  @DisplayName("보낸 회원과 받은 회원으로 좋아요를 찾고, 반대 방향의 좋아요는 찾지 않는다.")
  void findBySenderIdAndReceiverId() {
    Like like =
        em.persist(
            LikeFixture.builder()
                .senderId(1L)
                .receiverId(2L)
                .level(LikeLevel.HIGHLY_INTERESTED)
                .build());
    em.flush();
    em.clear();

    assertThat(likeCommandRepository.findBySenderIdAndReceiverId(1L, 2L))
        .hasValueSatisfying(found -> {
          assertThat(found.getId()).isEqualTo(like.getId());
          assertThat(found.getLevel()).isEqualTo(LikeLevel.HIGHLY_INTERESTED);
        });
    assertThat(likeCommandRepository.findBySenderIdAndReceiverId(2L, 1L)).isEmpty();
  }

  @Test
  @DisplayName("보낸 회원과 받은 회원 목록으로 좋아요를 찾고, 목록 밖이나 다른 회원이 보낸 좋아요는 찾지 않는다.")
  void findAllBySenderIdAndReceiverIdIn() {
    Like first = em.persist(LikeFixture.builder().senderId(1L).receiverId(2L).build());
    Like second = em.persist(LikeFixture.builder().senderId(1L).receiverId(3L).build());
    em.persist(LikeFixture.builder().senderId(1L).receiverId(4L).build());
    em.persist(LikeFixture.builder().senderId(5L).receiverId(2L).build());
    em.flush();
    em.clear();

    assertThat(likeCommandRepository.findAllBySenderIdAndReceiverIdIn(1L, List.of(2L, 3L, 9L)))
        .extracting(Like::getId)
        .containsExactlyInAnyOrder(first.getId(), second.getId());
  }
}
