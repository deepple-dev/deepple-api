package deepple.deepple.member.command.application.introduction;

import deepple.deepple.block.application.required.BlockRepository;
import deepple.deepple.datingexam.domain.AnswerPersonalityType;
import deepple.deepple.member.command.application.introduction.exception.InvalidPersonalityIntroductionUnlockException;
import deepple.deepple.member.command.domain.introduction.IntroductionType;
import deepple.deepple.member.command.domain.introduction.MemberIntroduction;
import deepple.deepple.member.command.domain.introduction.MemberIntroductionCommandRepository;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.query.introduction.intra.PersonalityIntroductionOpenState;
import deepple.deepple.member.query.introduction.intra.PersonalityIntroductionRedisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalityIntroductionUnlockServiceTest {

    private static final AnswerPersonalityType TYPE = AnswerPersonalityType.STIMULATING_ADVENTURER;

    @InjectMocks
    private PersonalityIntroductionUnlockService personalityIntroductionUnlockService;

    @Mock
    private MemberCommandRepository memberCommandRepository;
    @Mock
    private MemberIntroductionCommandRepository memberIntroductionCommandRepository;
    @Mock
    private BlockRepository blockRepository;
    @Mock
    private PersonalityIntroductionRedisRepository personalityIntroductionRedisRepository;

    @Test
    @DisplayName("오픈된 상태가 없으면 예외를 던진다.")
    void throwsWhenNotOpened() {
        long memberId = 1L;
        long targetMemberId = 2L;
        when(personalityIntroductionRedisRepository.findOpenState(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId))
            .isInstanceOf(InvalidPersonalityIntroductionUnlockException.class);
    }

    @Test
    @DisplayName("오픈된 목록에 없는 대상이면 예외를 던진다.")
    void throwsWhenTargetNotInOpenedList() {
        long memberId = 1L;
        long targetMemberId = 99L;
        when(personalityIntroductionRedisRepository.findOpenState(memberId))
            .thenReturn(Optional.of(new PersonalityIntroductionOpenState(TYPE, List.of(2L, 3L, 4L), LocalDateTime.now())));

        assertThatThrownBy(() -> personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId))
            .isInstanceOf(InvalidPersonalityIntroductionUnlockException.class);
    }

    @Test
    @DisplayName("오픈된 유형과 요청 유형이 다르면 예외를 던진다.")
    void throwsWhenTypeMismatch() {
        long memberId = 1L;
        long targetMemberId = 2L;
        when(personalityIntroductionRedisRepository.findOpenState(memberId))
            .thenReturn(Optional.of(new PersonalityIntroductionOpenState(AnswerPersonalityType.RATIONAL_REALIST,
                List.of(2L, 3L, 4L), LocalDateTime.now())));

        assertThatThrownBy(() -> personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId))
            .isInstanceOf(InvalidPersonalityIntroductionUnlockException.class);
    }

    @Test
    @DisplayName("이미 언락된 대상이면 과금 없이 멱등하게 처리한다.")
    void idempotentWhenAlreadyUnlocked() {
        long memberId = 1L;
        long targetMemberId = 2L;
        when(personalityIntroductionRedisRepository.findOpenState(memberId))
            .thenReturn(Optional.of(new PersonalityIntroductionOpenState(TYPE, List.of(2L, 3L, 4L), LocalDateTime.now())));
        when(personalityIntroductionRedisRepository.findUnlockedMemberIds(memberId)).thenReturn(Set.of(2L));

        personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId);

        verify(memberIntroductionCommandRepository, never()).save(any());
        verify(personalityIntroductionRedisRepository, never()).addUnlockedMemberId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("이미 소개 레코드가 있으면(Redis 유실 등) 과금 없이 멱등하게 처리한다.")
    void idempotentWhenAlreadyIntroducedInDb() {
        long memberId = 1L;
        long targetMemberId = 2L;
        when(personalityIntroductionRedisRepository.findOpenState(memberId))
            .thenReturn(Optional.of(new PersonalityIntroductionOpenState(TYPE, List.of(2L, 3L, 4L), LocalDateTime.now())));
        when(personalityIntroductionRedisRepository.findUnlockedMemberIds(memberId)).thenReturn(Set.of());
        when(memberIntroductionCommandRepository.existsByMemberIdAndIntroducedMemberId(memberId, targetMemberId))
            .thenReturn(true);

        personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId);

        verify(memberIntroductionCommandRepository, never()).save(any());
    }

    @Test
    @DisplayName("해당 유형의 첫 언락은 하트 차감 없이(ofWithoutCharge) 소개를 생성한다.")
    void firstUnlockIsFree() {
        long memberId = 1L;
        long targetMemberId = 2L;
        givenValidTarget(memberId, targetMemberId);
        when(personalityIntroductionRedisRepository.findOpenState(memberId))
            .thenReturn(Optional.of(new PersonalityIntroductionOpenState(TYPE, List.of(2L, 3L, 4L), LocalDateTime.now())));
        when(personalityIntroductionRedisRepository.findUnlockedMemberIds(memberId)).thenReturn(Set.of());

        try (MockedStatic<MemberIntroduction> mocked = mockStatic(MemberIntroduction.class)) {
            MemberIntroduction introduction = mock(MemberIntroduction.class);
            mocked.when(() -> MemberIntroduction.ofWithoutCharge(memberId, targetMemberId, IntroductionType.PERSONALITY))
                .thenReturn(introduction);

            personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId);

            mocked.verify(() -> MemberIntroduction.ofWithoutCharge(memberId, targetMemberId,
                IntroductionType.PERSONALITY));
            mocked.verify(() -> MemberIntroduction.of(anyLong(), anyLong(), any()), never());
            verify(memberIntroductionCommandRepository).save(introduction);
            verify(personalityIntroductionRedisRepository).addUnlockedMemberId(memberId, targetMemberId);
        }
    }

    @Test
    @DisplayName("첫 언락 이후에는 하트를 차감하며(of) 소개를 생성한다.")
    void subsequentUnlockCharges() {
        long memberId = 1L;
        long targetMemberId = 3L;
        givenValidTarget(memberId, targetMemberId);
        when(personalityIntroductionRedisRepository.findOpenState(memberId))
            .thenReturn(Optional.of(new PersonalityIntroductionOpenState(TYPE, List.of(2L, 3L, 4L), LocalDateTime.now())));
        when(personalityIntroductionRedisRepository.findUnlockedMemberIds(memberId)).thenReturn(Set.of(2L));

        try (MockedStatic<MemberIntroduction> mocked = mockStatic(MemberIntroduction.class)) {
            MemberIntroduction introduction = mock(MemberIntroduction.class);
            mocked.when(() -> MemberIntroduction.of(memberId, targetMemberId, IntroductionType.PERSONALITY))
                .thenReturn(introduction);

            personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId);

            mocked.verify(() -> MemberIntroduction.of(memberId, targetMemberId, IntroductionType.PERSONALITY));
            mocked.verify(() -> MemberIntroduction.ofWithoutCharge(anyLong(), anyLong(), any()), never());
            verify(memberIntroductionCommandRepository).save(introduction);
            verify(personalityIntroductionRedisRepository).addUnlockedMemberId(memberId, targetMemberId);
        }
    }

    private void givenValidTarget(long memberId, long targetMemberId) {
        Member target = mock(Member.class);
        when(target.isActive()).thenReturn(true);
        when(memberCommandRepository.findById(targetMemberId)).thenReturn(Optional.of(target));
        when(blockRepository.existsByBlockerIdAndBlockedId(memberId, targetMemberId)).thenReturn(false);
        when(blockRepository.existsByBlockerIdAndBlockedId(targetMemberId, memberId)).thenReturn(false);
    }
}
