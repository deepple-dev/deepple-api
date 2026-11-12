package kr.co.deepple.member.query.introduction.intra;


import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.member.command.domain.introduction.MemberIdeal;
import kr.co.deepple.member.command.domain.member.*;
import kr.co.deepple.member.fixture.MemberIdealFixture;
import kr.co.deepple.member.query.introduction.application.MemberIdealView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MemberIdealQueryRepositoryTest extends RepositoryTestSupport {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberIdealQueryRepository memberIdealQueryRepository;

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
        Hobby hobby1 = Hobby.ANIMATION;
        Hobby hobby2 = Hobby.BOARD_GAMES;
        Set<Hobby> hobbies = Set.of(hobby1, hobby2);
        Set<City> cities = Set.of(City.SEOUL);
        Religion religion = Religion.CHRISTIAN;
        SmokingStatus smokingStatus = SmokingStatus.NONE;
        DrinkingStatus drinkingStatus = DrinkingStatus.NONE;
        MemberIdeal memberIdeal = MemberIdealFixture.builder()
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

        // when
        MemberIdealView memberIdealView = memberIdealQueryRepository.findMemberIdealByMemberId(memberId).get();

        // then
        assertThat(memberIdealView.minAge()).isEqualTo(minAge);
        assertThat(memberIdealView.maxAge()).isEqualTo(maxAge);
        assertThat(memberIdealView.hobbies()).containsExactlyInAnyOrder(
            hobbies.stream().map(Hobby::name).toArray(String[]::new));
        assertThat(memberIdealView.cities()).containsExactlyInAnyOrder(
            cities.stream().map(City::name).toArray(String[]::new));
        assertThat(memberIdealView.religion()).isEqualTo(religion.name());
        assertThat(memberIdealView.smokingStatus()).isEqualTo(smokingStatus.name());
        assertThat(memberIdealView.drinkingStatus()).isEqualTo(drinkingStatus.name());
    }
}
