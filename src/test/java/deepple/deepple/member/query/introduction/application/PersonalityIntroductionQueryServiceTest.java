package deepple.deepple.member.query.introduction.application;

import deepple.deepple.datingexam.application.provided.PersonalityTypeMemberFinder;
import deepple.deepple.datingexam.domain.AnswerPersonalityType;
import deepple.deepple.member.command.application.introduction.exception.PersonalityIntroductionAlreadyOpenedException;
import deepple.deepple.member.query.introduction.intra.IntroductionQueryRepository;
import deepple.deepple.member.query.introduction.intra.PersonalityIntroductionOpenState;
import deepple.deepple.member.query.introduction.intra.PersonalityIntroductionRedisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalityIntroductionQueryServiceTest {

    private static final AnswerPersonalityType TYPE = AnswerPersonalityType.STIMULATING_ADVENTURER;

    @InjectMocks
    private PersonalityIntroductionQueryService personalityIntroductionQueryService;

    @Mock
    private PersonalityTypeMemberFinder personalityTypeMemberFinder;
    @Mock
    private IntroductionQueryService introductionQueryService;
    @Mock
    private IntroductionQueryRepository introductionQueryRepository;
    @Mock
    private PersonalityIntroductionRedisRepository personalityIntroductionRedisRepository;

    @Test
    @DisplayName("같은 유형이 이미 열려 있으면 후보를 재계산하지 않고 고정된 목록을 반환한다.")
    void returnsFrozenListWhenSameTypeOpened() {
        long memberId = 1L;
        when(personalityIntroductionRedisRepository.findOpenState(memberId))
            .thenReturn(Optional.of(new PersonalityIntroductionOpenState(TYPE, List.of(2L, 3L, 4L), LocalDateTime.now())));

        personalityIntroductionQueryService.open(memberId, TYPE);

        verify(introductionQueryService).findMemberIntroductionProfileViews(eq(memberId), eq(Set.of(2L, 3L, 4L)));
        verify(personalityTypeMemberFinder, never()).findMemberIdsByDominantType(anyLong(), any());
        verify(personalityIntroductionRedisRepository, never()).saveOpenState(anyLong(), any());
    }

    @Test
    @DisplayName("다른 유형이 이미 열려 있으면 예외를 던진다.")
    void throwsWhenDifferentTypeOpened() {
        long memberId = 1L;
        when(personalityIntroductionRedisRepository.findOpenState(memberId))
            .thenReturn(Optional.of(new PersonalityIntroductionOpenState(AnswerPersonalityType.RATIONAL_REALIST,
                List.of(2L, 3L, 4L), LocalDateTime.now())));

        assertThatThrownBy(() -> personalityIntroductionQueryService.open(memberId, TYPE))
            .isInstanceOf(PersonalityIntroductionAlreadyOpenedException.class);
    }

    @Test
    @DisplayName("오픈되지 않은 상태에서 후보가 있으면 최신순 최대 3명을 저장하고 반환한다.")
    void savesAndReturnsWhenNotOpened() {
        long memberId = 1L;
        when(personalityIntroductionRedisRepository.findOpenState(memberId)).thenReturn(Optional.empty());
        when(personalityTypeMemberFinder.findMemberIdsByDominantType(memberId, TYPE))
            .thenReturn(List.of(10L, 9L, 8L, 7L));
        givenNoExclusions(memberId);

        personalityIntroductionQueryService.open(memberId, TYPE);

        ArgumentCaptor<PersonalityIntroductionOpenState> stateCaptor =
            ArgumentCaptor.forClass(PersonalityIntroductionOpenState.class);
        verify(personalityIntroductionRedisRepository).saveOpenState(eq(memberId), stateCaptor.capture());
        assertThat(stateCaptor.getValue().type()).isEqualTo(TYPE);
        assertThat(stateCaptor.getValue().memberIds()).isEqualTo(List.of(10L, 9L, 8L));
        assertThat(stateCaptor.getValue().openedAt()).isNotNull();
        verify(introductionQueryService).findMemberIntroductionProfileViews(eq(memberId), eq(Set.of(10L, 9L, 8L)));
    }

    @Test
    @DisplayName("후보가 0명이면 오픈을 소진하지 않고(저장 X) 빈 목록을 반환한다.")
    void emptyDoesNotConsumeOpen() {
        long memberId = 1L;
        when(personalityIntroductionRedisRepository.findOpenState(memberId)).thenReturn(Optional.empty());
        when(personalityTypeMemberFinder.findMemberIdsByDominantType(memberId, TYPE)).thenReturn(List.of());
        givenNoExclusions(memberId);

        assertThat(personalityIntroductionQueryService.open(memberId, TYPE)).isEmpty();

        verify(personalityIntroductionRedisRepository, never()).saveOpenState(anyLong(), any());
        verify(introductionQueryService, never()).findMemberIntroductionProfileViews(anyLong(), any());
    }

    @Test
    @DisplayName("이미 소개/매칭된 상대는 제외한 뒤 최대 3명을 선별한다.")
    void excludesAlreadyIntroduced() {
        long memberId = 1L;
        when(personalityIntroductionRedisRepository.findOpenState(memberId)).thenReturn(Optional.empty());
        when(personalityTypeMemberFinder.findMemberIdsByDominantType(memberId, TYPE))
            .thenReturn(List.of(10L, 9L, 8L, 7L));
        when(introductionQueryRepository.findAllMatchRequestedMemberId(memberId)).thenReturn(Set.of());
        when(introductionQueryRepository.findAllMatchRequestingMemberId(memberId)).thenReturn(Set.of());
        when(introductionQueryRepository.findAllIntroducedMemberId(memberId)).thenReturn(Set.of(9L));

        personalityIntroductionQueryService.open(memberId, TYPE);

        ArgumentCaptor<PersonalityIntroductionOpenState> stateCaptor =
            ArgumentCaptor.forClass(PersonalityIntroductionOpenState.class);
        verify(personalityIntroductionRedisRepository).saveOpenState(eq(memberId), stateCaptor.capture());
        assertThat(stateCaptor.getValue().type()).isEqualTo(TYPE);
        assertThat(stateCaptor.getValue().memberIds()).isEqualTo(List.of(10L, 8L, 7L));
    }

    @Test
    @DisplayName("오픈 이력이 있으면 오픈된 유형과 오픈 시각을 반환한다.")
    void returnsOpenStatusWhenOpened() {
        long memberId = 1L;
        LocalDateTime openedAt = LocalDateTime.of(2026, 8, 8, 12, 0);
        when(personalityIntroductionRedisRepository.findOpenState(memberId))
            .thenReturn(Optional.of(new PersonalityIntroductionOpenState(TYPE, List.of(2L, 3L, 4L), openedAt)));

        Optional<PersonalityIntroductionOpenStatusView> openStatus =
            personalityIntroductionQueryService.findOpenStatus(memberId);

        assertThat(openStatus).contains(new PersonalityIntroductionOpenStatusView(TYPE, openedAt));
    }

    @Test
    @DisplayName("오픈 이력이 없으면 빈 값을 반환한다.")
    void returnsEmptyOpenStatusWhenNotOpened() {
        long memberId = 1L;
        when(personalityIntroductionRedisRepository.findOpenState(memberId)).thenReturn(Optional.empty());

        Optional<PersonalityIntroductionOpenStatusView> openStatus =
            personalityIntroductionQueryService.findOpenStatus(memberId);

        assertThat(openStatus).isEmpty();
    }

    private void givenNoExclusions(long memberId) {
        when(introductionQueryRepository.findAllMatchRequestedMemberId(memberId)).thenReturn(Set.of());
        when(introductionQueryRepository.findAllMatchRequestingMemberId(memberId)).thenReturn(Set.of());
        when(introductionQueryRepository.findAllIntroducedMemberId(memberId)).thenReturn(Set.of());
    }
}
