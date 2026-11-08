package deepple.deepple.member.command.domain.introduction;

import deepple.deepple.member.command.domain.introduction.vo.AgeRange;
import deepple.deepple.member.command.domain.member.*;
import deepple.deepple.member.fixture.MemberIdealFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberIdealTest {

    @Nested
    @DisplayName("init 메서드 테스트")
    class InitTest {

        @Test
        @DisplayName("memberId가 null이면 예외를 던진다.")
        void throwsExceptionWhenMemberIdIsNull() {
            // given
            Long memberId = null;

            // when, then
            assertThatThrownBy(() -> MemberIdeal.init(memberId))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("정상적으로 MemberIdeal 객체를 생성한다.")
        void createsMemberIdealObject() {
            // given
            Long memberId = 1L;

            // when
            MemberIdeal memberIdeal = MemberIdeal.init(memberId);

            // then
            assertThat(memberIdeal.getMemberId()).isEqualTo(memberId);
            assertThat(memberIdeal.getAgeRange()).isEqualTo(AgeRange.init());
            assertThat(memberIdeal.getHobbies()).isEmpty();
            assertThat(memberIdeal.getCities()).isEmpty();
            assertThat(memberIdeal.getReligion()).isNull();
            assertThat(memberIdeal.getSmokingStatus()).isNull();
            assertThat(memberIdeal.getDrinkingStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("update 메서드 테스트")
    class UpdateTest {

        private Set<Hobby> hobbies;
        private AgeRange ageRange;
        private Set<City> cities;
        private Religion religion;
        private SmokingStatus smokingStatus;
        private DrinkingStatus drinkingStatus;

        @BeforeEach
        void setUp() {
            ageRange = AgeRange.of(20, 30);
            cities = Set.of(City.SEOUL);
            religion = Religion.CHRISTIAN;
            smokingStatus = SmokingStatus.VAPE;
            drinkingStatus = DrinkingStatus.SOCIAL;
            hobbies = Set.of(Hobby.DANCE, Hobby.ANIMATION);
        }

        @Test
        @DisplayName("ageRange가 null이면 예외를 던진다.")
        void throwsExceptionWhenAgeRangeIsNull() {
            // given
            MemberIdeal memberIdeal = MemberIdealFixture.builder().memberId(1L).build();
            ageRange = null;

            // when, then
            assertThatThrownBy(
                () -> memberIdeal.update(ageRange, hobbies, cities, religion, smokingStatus, drinkingStatus))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hobbies가 null이면 예외를 던진다.")
        void throwsExceptionWhenHobbiesAreNull() {
            // given
            MemberIdeal memberIdeal = MemberIdealFixture.builder().memberId(1L).build();
            hobbies = null;

            // when, then
            assertThatThrownBy(
                () -> memberIdeal.update(ageRange, hobbies, cities, religion, smokingStatus, drinkingStatus))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("cities가 null이면 예외를 던진다.")
        void throwsExceptionWhenCitiesIsNull() {
            // given
            MemberIdeal memberIdeal = MemberIdealFixture.builder().memberId(1L).build();
            cities = null;

            // when, then
            assertThatThrownBy(
                () -> memberIdeal.update(ageRange, hobbies, cities, religion, smokingStatus, drinkingStatus))
                .isInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"religion", "smokingStatus", "drinkingStatus", "none"})
        @DisplayName("ageRange, hobbies, cities 외의 다른 파라미터가 null이면 update한다.")
        void updatesMemberIdeal(String nullParameter) {
            // given
            MemberIdeal memberIdeal = MemberIdealFixture.builder().memberId(1L).build();
            if (nullParameter.equals("religion")) {
                religion = null;
            } else if (nullParameter.equals("smokingStatus")) {
                smokingStatus = null;
            } else if (nullParameter.equals("drinkingStatus")) {
                drinkingStatus = null;
            } else if (nullParameter.equals("none")) {
                // do nothing
            }

            // when
            memberIdeal.update(ageRange, hobbies, cities, religion, smokingStatus, drinkingStatus);

            // then
            assertThat(memberIdeal.getAgeRange()).isEqualTo(ageRange);
            assertThat(memberIdeal.getHobbies()).isEqualTo(hobbies);
            assertThat(memberIdeal.getCities()).isEqualTo(cities);
            assertThat(memberIdeal.getReligion()).isEqualTo(religion);
            assertThat(memberIdeal.getSmokingStatus()).isEqualTo(smokingStatus);
            assertThat(memberIdeal.getDrinkingStatus()).isEqualTo(drinkingStatus);
        }
    }
}