package kr.co.deepple.like.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;

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
}
