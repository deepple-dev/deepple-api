package deepple.deepple.member.command.application.introduction;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.introduction.MemberIdeal;
import deepple.deepple.member.command.domain.introduction.MemberIdealCommandRepository;
import deepple.deepple.member.command.domain.introduction.vo.AgeRange;
import deepple.deepple.member.command.domain.member.*;
import deepple.deepple.member.fixture.MemberIdealFixture;
import deepple.deepple.member.presentation.introduction.dto.MemberIdealUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberIdealServiceTest {

    @InjectMocks
    private MemberIdealService memberIdealService;

    @Mock
    private MemberIdealCommandRepository memberIdealCommandRepository;


    @Nested
    @DisplayName("update 메서드 테스트")
    class UpdateTest {
        private Set<Hobby> hobbies;
        private AgeRange ageRange;
        private Set<City> cities;
        private Religion religion;
        private SmokingStatus smokingStatus;
        private DrinkingStatus drinkingStatus;
        private MemberIdealUpdateRequest request;

        @BeforeEach
        void setUp() {
            ageRange = AgeRange.of(20, 30);
            cities = Set.of(City.SEOUL);
            religion = Religion.CHRISTIAN;
            smokingStatus = SmokingStatus.VAPE;
            drinkingStatus = DrinkingStatus.SOCIAL;
            hobbies = Set.of(Hobby.DANCE, Hobby.WINE);
            request = new MemberIdealUpdateRequest(
                ageRange.getMinAge(),
                ageRange.getMaxAge(),
                cities.stream().map(City::name).collect(Collectors.toSet()),
                religion.name(),
                smokingStatus.name(),
                drinkingStatus.name(),
                hobbies.stream().map(Hobby::name).collect(Collectors.toSet())
            );
        }


        @Test
        @DisplayName("memberIdeal이 존재하지 않는 경우 예외를 던진다.")
        void throwExceptionWhenMemberIdealIsNotExists() {
            // given
            long memberId = 1L;
            when(memberIdealCommandRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            // when && then
            assertThatThrownBy(() -> memberIdealService.update(request, memberId))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_IDEAL_NOT_FOUND);
        }

        @Test
        @DisplayName("memberIdeal이 존재하는 경우 memberIdeal을 업데이트한다.")
        void updateMemberIdeal() {
            long memberId = 1L;
            MemberIdeal memberIdeal = MemberIdealFixture.builder().memberId(memberId).build();
            when(memberIdealCommandRepository.findByMemberId(memberId)).thenReturn(Optional.of(memberIdeal));

            // when
            memberIdealService.update(request, memberId);

            // then
            assertThat(memberIdeal.getAgeRange()).isEqualTo(ageRange);
            assertThat(memberIdeal.getHobbies()).isEqualTo(hobbies);
            assertThat(memberIdeal.getCities()).isEqualTo(cities);
            assertThat(memberIdeal.getReligion()).isEqualTo(religion);
            assertThat(memberIdeal.getSmokingStatus()).isEqualTo(smokingStatus);
            assertThat(memberIdeal.getDrinkingStatus()).isEqualTo(drinkingStatus);
        }
    }

    @Nested
    @DisplayName("initIfAbsent 메서드 테스트")
    class InitIfAbsentTest {
        @Test
        @DisplayName("memberIdeal이 이미 존재하는 경우 저장하지 않는다.")
        void skipWhenMemberIdealAlreadyExists() {
            // given
            long memberId = 1L;
            when(memberIdealCommandRepository.existsByMemberId(memberId)).thenReturn(true);

            // when
            memberIdealService.initIfAbsent(memberId);

            // then
            verify(memberIdealCommandRepository, never()).save(any());
        }

        @Test
        @DisplayName("memberIdeal이 존재하지 않는 경우 memberIdeal을 생성한다.")
        void createMemberIdeal() {
            // given
            long memberId = 1L;
            when(memberIdealCommandRepository.existsByMemberId(memberId)).thenReturn(false);

            try (MockedStatic<MemberIdeal> memberIdealMock = mockStatic(MemberIdeal.class)) {
                MemberIdeal memberIdeal = mock(MemberIdeal.class);
                memberIdealMock.when(() -> MemberIdeal.init(memberId)).thenReturn(memberIdeal);

                // when
                memberIdealService.initIfAbsent(memberId);

                // then
                verify(memberIdealCommandRepository).save(memberIdeal);
            }
        }
    }
}