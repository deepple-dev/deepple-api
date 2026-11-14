package kr.co.deepple.recommendation.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.recommendation.fixture.MemberIntroductionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class IntroductionQueryRepositoryImplTest extends RepositoryTestSupport {

  @Autowired private TestEntityManager entityManager;

  @Autowired private IntroductionQueryRepositoryImpl introductionQueryRepository;

  @Test
  @DisplayName("내가 소개받은 회원만 찾고, 나를 소개받은 회원이나 다른 회원의 소개는 찾지 않는다.")
  void findAllIntroducedMemberId() {
    persistIntroduction(1L, 2L);
    persistIntroduction(1L, 3L);
    persistIntroduction(4L, 1L);
    persistIntroduction(5L, 6L);
    entityManager.flush();

    assertThat(introductionQueryRepository.findAllIntroducedMemberId(1L))
        .containsExactlyInAnyOrder(2L, 3L);
  }

  @Test
  @DisplayName("회원 목록 중 내가 소개받은 회원만 찾고, 반대 방향의 소개와 목록 밖의 소개는 찾지 않는다.")
  void findIntroducedMemberIds() {
    persistIntroduction(1L, 2L);
    persistIntroduction(1L, 3L);
    persistIntroduction(4L, 1L);
    persistIntroduction(5L, 4L);
    entityManager.flush();

    assertThat(introductionQueryRepository.findIntroducedMemberIds(1L, List.of(2L, 4L, 9L)))
        .containsExactly(2L);
  }

  @Test
  @DisplayName("회원 목록이 비어 있으면 빈 결과를 돌려준다.")
  void returnEmptyWhenNoMemberIds() {
    persistIntroduction(1L, 2L);
    entityManager.flush();

    assertThat(introductionQueryRepository.findIntroducedMemberIds(1L, Set.of())).isEmpty();
  }

  private void persistIntroduction(long memberId, long introducedMemberId) {
    entityManager.persist(
        MemberIntroductionFixture.builder()
            .memberId(memberId)
            .introducedMemberId(introducedMemberId)
            .build());
  }
}
