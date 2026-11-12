package kr.co.deepple.admin.command.application.suspension;

import kr.co.deepple.admin.command.domain.suspension.Suspension;
import kr.co.deepple.admin.command.domain.suspension.SuspensionCommandRepository;
import kr.co.deepple.admin.command.domain.suspension.SuspensionPolicy;
import kr.co.deepple.admin.command.domain.suspension.SuspensionStatus;
import kr.co.deepple.admin.fixture.SuspensionFixture;
import kr.co.deepple.admin.presentation.suspension.SuspendRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SuspensionServiceTest {

    @Mock
    SuspensionCommandRepository suspensionCommandRepository;

    @Mock
    SuspensionPolicy suspensionPolicy;

    @Mock
    TaskScheduler taskScheduler;

    @InjectMocks
    SuspensionService suspensionService;

    @Test
    @DisplayName("suspendByAdmin(): 기존 Suspension 없으면 새로 생성된 상태를 저장한다")
    void suspendByAdminWhenNoExistingSuspension() {
        // given
        var adminId = 10L;
        var memberId = 20L;
        var request = new SuspendRequest("PERMANENT");
        given(suspensionCommandRepository.findByMemberId(memberId))
            .willReturn(Optional.empty());

        // when
        suspensionService.suspendByAdmin(adminId, memberId, request);

        // then
        var captor = ArgumentCaptor.forClass(Suspension.class);
        verify(suspensionCommandRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getAdminId()).isEqualTo(adminId);
        assertThat(saved.getMemberId()).isEqualTo(memberId);
        assertThat(saved.getStatus()).isEqualTo(SuspensionStatus.PERMANENT);
    }

    @Test
    @DisplayName("suspendByAdmin(): 기존 Suspension 있으면 해당 엔티티의 상태가 변경된다")
    void suspendByAdminWhenExistingSuspension() {
        // given
        var adminId = 11L;
        var memberId = 21L;
        var existing = SuspensionFixture.builder()
            .adminId(1L)
            .memberId(memberId)
            .status(SuspensionStatus.TEMPORARY)
            .build();
        var request = new SuspendRequest("PERMANENT");
        given(suspensionCommandRepository.findByMemberId(memberId))
            .willReturn(Optional.of(existing));

        // when
        suspensionService.suspendByAdmin(adminId, memberId, request);

        // then
        assertThat(existing.getAdminId()).isEqualTo(adminId);
        assertThat(existing.getStatus()).isEqualTo(SuspensionStatus.PERMANENT);
        assertThat(existing.getExpireAt()).isNull();
    }

    @Test
    @DisplayName("suspendByWarningCount(): policy가 TEMPORARY 반환 시 새로 생성된 Temporary 상태를 저장한다")
    void suspendByWarningCountWhenPolicyReturnsTemporary() {
        // given
        var adminId = 12L;
        var memberId = 22L;
        var warningCount = 2L;
        given(suspensionPolicy.evaluate(warningCount))
            .willReturn(SuspensionStatus.TEMPORARY);
        given(suspensionCommandRepository.findByMemberId(memberId))
            .willReturn(Optional.empty());

        // when
        suspensionService.suspendByWarningCount(adminId, memberId, warningCount);

        // then
        var captor = ArgumentCaptor.forClass(Suspension.class);
        verify(suspensionCommandRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getAdminId()).isEqualTo(adminId);
        assertThat(saved.getMemberId()).isEqualTo(memberId);
        assertThat(saved.getStatus()).isEqualTo(SuspensionStatus.TEMPORARY);
        assertThat(saved.getExpireAt()).isNotNull();
    }

    @Test
    @DisplayName("suspendByWarningCount(): policy가 PERMANENT 반환 시 새로 생성된 PERMANENT 상태를 저장한다")
    void suspendByWarningCountWhenPolicyReturnsNewPermanent() {
        // given
        var adminId = 12L;
        var memberId = 22L;
        var warningCount = 2L;
        given(suspensionPolicy.evaluate(warningCount))
            .willReturn(SuspensionStatus.PERMANENT);
        given(suspensionCommandRepository.findByMemberId(memberId))
            .willReturn(Optional.empty());

        // when
        suspensionService.suspendByWarningCount(adminId, memberId, warningCount);

        // then
        var captor = ArgumentCaptor.forClass(Suspension.class);
        verify(suspensionCommandRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getAdminId()).isEqualTo(adminId);
        assertThat(saved.getMemberId()).isEqualTo(memberId);
        assertThat(saved.getStatus()).isEqualTo(SuspensionStatus.PERMANENT);
        assertThat(saved.getExpireAt()).isNull();
    }

    @Test
    @DisplayName("suspendByWarningCount(): policy가 PERMANENT 반환 시 기존 엔티티가 Permanent 상태로 업데이트된다")
    void suspendByWarningCountWhenPolicyReturnsPermanent() {
        // given
        var adminId = 13L;
        var memberId = 23L;
        var warningCount = 5L;
        // 기존 엔티티를 임시 정지 상태로 생성
        var existing = SuspensionFixture.builder()
            .adminId(1L)
            .memberId(memberId)
            .status(SuspensionStatus.TEMPORARY)
            .build();
        given(suspensionPolicy.evaluate(warningCount))
            .willReturn(SuspensionStatus.PERMANENT);
        given(suspensionCommandRepository.findByMemberId(memberId))
            .willReturn(Optional.of(existing));

        // when
        suspensionService.suspendByWarningCount(adminId, memberId, warningCount);

        // then
        assertThat(existing.getAdminId()).isEqualTo(adminId);
        assertThat(existing.getStatus()).isEqualTo(SuspensionStatus.PERMANENT);
        assertThat(existing.getExpireAt()).isNull();
    }
}
