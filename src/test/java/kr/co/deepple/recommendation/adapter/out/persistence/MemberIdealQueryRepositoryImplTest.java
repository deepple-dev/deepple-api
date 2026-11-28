package kr.co.deepple.recommendation.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.recommendation.application.dto.MemberIdealView;
import kr.co.deepple.recommendation.domain.MemberIdeal;
import kr.co.deepple.recommendation.fixture.MemberIdealFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class MemberIdealQueryRepositoryImplTest extends RepositoryTestSupport {

  @Autowired private TestEntityManager entityManager;

  @Autowired private MemberIdealQueryRepositoryImpl memberIdealQueryRepository;

  @Test
  @DisplayName("memberIdeal이 존재하지 않는 경우 Optional.empty()를 반환한다.")
  void returnOptionalEmptyWhenMemberIdealIsNotExists() {
    // given
    long memberId = 1L;

    // when
    var memberIdeal = memberIdealQueryRepository.findMemberIdealByMemberId(memberId);

    // then
    assertThat(memberIdeal).isEmpty();
  }

  @Test
  @DisplayName("memberIdeal이 존재하는 경우 MemberIdealView를 반환한다.")
  void returnMemberIdealViewWhenMemberIdealIsExists() {
    // given
    long memberId = 1L;
    int minAge = 20;
    int maxAge = 30;
    Set<String> hobbies = Set.of("ANIMATION", "BOARD_GAMES");
    Set<String> cities = Set.of("SEOUL");
    String religion = "CHRISTIAN";
    String smokingStatus = "NONE";
    String drinkingStatus = "NONE";
    MemberIdeal memberIdeal =
        MemberIdealFixture.builder()
            .memberId(memberId)
            .minAge(minAge)
            .maxAge(maxAge)
            .hobbies(new HashSet<>(hobbies))
            .cities(new HashSet<>(cities))
            .religion(religion)
            .smokingStatus(smokingStatus)
            .drinkingStatus(drinkingStatus)
            .build();
    entityManager.persist(memberIdeal);
    entityManager.flush();
    entityManager.clear();

    // when
    MemberIdealView memberIdealView =
        memberIdealQueryRepository.findMemberIdealByMemberId(memberId).get();

    // then
    assertThat(memberIdealView.minAge()).isEqualTo(minAge);
    assertThat(memberIdealView.maxAge()).isEqualTo(maxAge);
    assertThat(memberIdealView.hobbies()).containsExactlyInAnyOrderElementsOf(hobbies);
    assertThat(memberIdealView.cities()).containsExactlyInAnyOrderElementsOf(cities);
    assertThat(memberIdealView.religion()).isEqualTo(religion);
    assertThat(memberIdealView.smokingStatus()).isEqualTo(smokingStatus);
    assertThat(memberIdealView.drinkingStatus()).isEqualTo(drinkingStatus);
  }

  @Test
  @DisplayName("나이 범위는 연 나이로 읽고, DB에는 옛 버전과 같은 한국식 나이(연 나이 + 1)로 저장한다.")
  void storesAgeRangeAsKoreanAge() {
    // given
    long memberId = 1L;
    MemberIdeal memberIdeal =
        MemberIdealFixture.builder().memberId(memberId).minAge(19).maxAge(29).build();
    entityManager.persist(memberIdeal);
    entityManager.flush();
    entityManager.clear();

    // when
    Object[] stored =
        (Object[])
            entityManager
                .getEntityManager()
                .createNativeQuery("select min_age, max_age from member_ideals where member_id = ?")
                .setParameter(1, memberId)
                .getSingleResult();
    MemberIdeal found = entityManager.find(MemberIdeal.class, memberIdeal.getId());
    MemberIdealView view = memberIdealQueryRepository.findMemberIdealByMemberId(memberId).get();

    // then
    assertThat(((Number) stored[0]).intValue()).isEqualTo(20);
    assertThat(((Number) stored[1]).intValue()).isEqualTo(30);
    assertThat(found.getAgeRange().getMinAge()).isEqualTo(19);
    assertThat(found.getAgeRange().getMaxAge()).isEqualTo(29);
    assertThat(view.minAge()).isEqualTo(19);
    assertThat(view.maxAge()).isEqualTo(29);
  }
}
