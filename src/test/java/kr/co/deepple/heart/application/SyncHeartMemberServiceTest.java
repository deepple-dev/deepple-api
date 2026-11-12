package kr.co.deepple.heart.application;

import kr.co.deepple.heart.application.port.in.SyncHeartMemberCommand;
import kr.co.deepple.heart.application.port.out.HeartMemberRepository;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.domain.Gender;
import kr.co.deepple.heart.domain.HeartMember;
import kr.co.deepple.heart.fixture.HeartMemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncHeartMemberServiceTest {
    private static final long MEMBER_ID = 1L;
    private static final Instant NOW = Instant.parse("2026-01-02T00:00:00Z");

    @Mock
    private HeartMemberRepository heartMemberRepository;

    @Mock
    private MemberHeartBalanceRepository memberHeartBalanceRepository;

    @InjectMocks
    private SyncHeartMemberService syncHeartMemberService;

    @Test
    @DisplayName("사본과 0짜리 잔액을 없을 때만 넣고, 잠근 사본에 값을 반영한다.")
    void insertIfAbsentAndUpdate() {
        // given
        HeartMember heartMember = HeartMemberFixture.builder().memberId(MEMBER_ID).build();
        when(heartMemberRepository.findByMemberIdForUpdate(MEMBER_ID)).thenReturn(Optional.of(heartMember));

        // when
        syncHeartMemberService.syncHeartMember(new SyncHeartMemberCommand(MEMBER_ID, "FEMALE", true, NOW));

        // then
        verify(heartMemberRepository).insertIfAbsent(eq(MEMBER_ID), eq("FEMALE"), eq(true), eq(NOW), any());
        verify(memberHeartBalanceRepository).insertIfAbsent(eq(MEMBER_ID), any());
        assertThat(heartMember.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(heartMember.isVip()).isTrue();
    }

    @Test
    @DisplayName("성별이 아직 없으면 비워 둔다.")
    void keepGenderEmptyWhenAbsent() {
        // given
        HeartMember heartMember = HeartMemberFixture.builder().memberId(MEMBER_ID).build();
        when(heartMemberRepository.findByMemberIdForUpdate(MEMBER_ID)).thenReturn(Optional.of(heartMember));

        // when
        syncHeartMemberService.syncHeartMember(new SyncHeartMemberCommand(MEMBER_ID, null, false, NOW));

        // then
        assertThat(heartMember.getGender()).isNull();
    }

    @Test
    @DisplayName("모르는 성별 값이면 예외가 발생하고 아무것도 넣지 않는다.")
    void throwWhenGenderIsUnknown() {
        assertThatThrownBy(() -> syncHeartMemberService.syncHeartMember(
            new SyncHeartMemberCommand(MEMBER_ID, "UNKNOWN", false, NOW)))
            .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(heartMemberRepository, memberHeartBalanceRepository);
    }
}
