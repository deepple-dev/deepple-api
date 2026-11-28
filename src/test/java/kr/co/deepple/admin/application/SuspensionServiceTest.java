package kr.co.deepple.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Optional;
import kr.co.deepple.admin.application.dto.SuspendRequest;
import kr.co.deepple.admin.application.port.out.SuspensionRepository;
import kr.co.deepple.admin.domain.Suspension;
import kr.co.deepple.admin.domain.SuspensionPolicy;
import kr.co.deepple.admin.domain.SuspensionStatus;
import kr.co.deepple.admin.event.MemberSuspendedEvent;
import kr.co.deepple.admin.event.MemberUnsuspendedEvent;
import kr.co.deepple.admin.fixture.SuspensionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class SuspensionServiceTest {

  @Mock SuspensionRepository suspensionRepository;

  @Mock SuspensionPolicy suspensionPolicy;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks SuspensionService suspensionService;

  @Test
  @DisplayName("suspendByAdmin(): 기존 Suspension 없으면 새로 생성된 상태를 저장한다")
  void suspendByAdminWhenNoExistingSuspension() {
    // given
    var adminId = 10L;
    var memberId = 20L;
    var request = new SuspendRequest("PERMANENT");
    given(suspensionRepository.findByMemberIdForUpdate(memberId)).willReturn(Optional.empty());

    // when
    suspensionService.suspendByAdmin(adminId, memberId, request);

    // then
    Mockito.verify(eventPublisher).publishEvent(Mockito.any(MemberSuspendedEvent.class));
    var captor = ArgumentCaptor.forClass(Suspension.class);
    verify(suspensionRepository).save(captor.capture());
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
    var existing =
        SuspensionFixture.builder()
            .adminId(1L)
            .memberId(memberId)
            .status(SuspensionStatus.TEMPORARY)
            .build();
    var request = new SuspendRequest("PERMANENT");
    given(suspensionRepository.findByMemberIdForUpdate(memberId)).willReturn(Optional.of(existing));

    // when
    suspensionService.suspendByAdmin(adminId, memberId, request);

    // then
    Mockito.verify(eventPublisher).publishEvent(Mockito.any(MemberSuspendedEvent.class));
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
    given(suspensionPolicy.evaluate(warningCount)).willReturn(SuspensionStatus.TEMPORARY);
    given(suspensionRepository.findByMemberIdForUpdate(memberId)).willReturn(Optional.empty());

    // when
    suspensionService.suspendByWarningCount(adminId, memberId, warningCount);

    // then
    Mockito.verify(eventPublisher).publishEvent(Mockito.any(MemberSuspendedEvent.class));
    var captor = ArgumentCaptor.forClass(Suspension.class);
    verify(suspensionRepository).save(captor.capture());
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
    given(suspensionPolicy.evaluate(warningCount)).willReturn(SuspensionStatus.PERMANENT);
    given(suspensionRepository.findByMemberIdForUpdate(memberId)).willReturn(Optional.empty());

    // when
    suspensionService.suspendByWarningCount(adminId, memberId, warningCount);

    // then
    Mockito.verify(eventPublisher).publishEvent(Mockito.any(MemberSuspendedEvent.class));
    var captor = ArgumentCaptor.forClass(Suspension.class);
    verify(suspensionRepository).save(captor.capture());
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
    var existing =
        SuspensionFixture.builder()
            .adminId(1L)
            .memberId(memberId)
            .status(SuspensionStatus.TEMPORARY)
            .build();
    given(suspensionPolicy.evaluate(warningCount)).willReturn(SuspensionStatus.PERMANENT);
    given(suspensionRepository.findByMemberIdForUpdate(memberId)).willReturn(Optional.of(existing));

    // when
    suspensionService.suspendByWarningCount(adminId, memberId, warningCount);

    // then
    Mockito.verify(eventPublisher).publishEvent(Mockito.any(MemberSuspendedEvent.class));
    assertThat(existing.getAdminId()).isEqualTo(adminId);
    assertThat(existing.getStatus()).isEqualTo(SuspensionStatus.PERMANENT);
    assertThat(existing.getExpireAt()).isNull();
  }

  @Test
  @DisplayName("suspendByAdmin(): 만료됐지만 해제되지 않은 일시 정지가 있으면 기간을 다시 시작한다")
  void suspendByAdminRestartsExpiredTemporary() {
    // given
    var memberId = 24L;
    var existing =
        SuspensionFixture.builder()
            .memberId(memberId)
            .expireAt(Instant.now().minusSeconds(60))
            .build();
    given(suspensionRepository.findByMemberIdForUpdate(memberId)).willReturn(Optional.of(existing));

    // when
    suspensionService.suspendByAdmin(14L, memberId, new SuspendRequest("TEMPORARY"));

    // then
    verify(eventPublisher).publishEvent(any(MemberSuspendedEvent.class));
    assertThat(existing.getAdminId()).isEqualTo(14L);
    assertThat(existing.getExpireAt()).isAfter(Instant.now());
  }

  @Test
  @DisplayName("releaseIfExpired(): 만료된 일시 정지는 관리자 해제처럼 삭제하고 해제 이벤트를 발행한다")
  void releaseExpiredTemporary() {
    // given
    var memberId = 30L;
    var now = Instant.now();
    var expired = SuspensionFixture.builder().memberId(memberId).expireAt(now).build();
    given(suspensionRepository.findByMemberIdForUpdate(memberId)).willReturn(Optional.of(expired));

    // when
    suspensionService.releaseIfExpired(memberId, now);

    // then
    verify(suspensionRepository).deleteByMemberId(memberId);
    var captor = ArgumentCaptor.forClass(MemberUnsuspendedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    assertThat(captor.getValue().memberId()).isEqualTo(memberId);
  }

  @Test
  @DisplayName("releaseIfExpired(): 아직 만료되지 않은 일시 정지는 해제하지 않는다")
  void doNotReleaseNotExpiredTemporary() {
    // given
    var memberId = 31L;
    var now = Instant.now();
    var active =
        SuspensionFixture.builder().memberId(memberId).expireAt(now.plusSeconds(1)).build();
    given(suspensionRepository.findByMemberIdForUpdate(memberId)).willReturn(Optional.of(active));

    // when
    suspensionService.releaseIfExpired(memberId, now);

    // then
    verify(suspensionRepository, never()).deleteByMemberId(anyLong());
    Mockito.verifyNoInteractions(eventPublisher);
  }

  @Test
  @DisplayName("releaseIfExpired(): 영구 정지는 해제하지 않는다")
  void doNotReleasePermanent() {
    // given
    var memberId = 32L;
    var permanent =
        SuspensionFixture.builder().memberId(memberId).status(SuspensionStatus.PERMANENT).build();
    given(suspensionRepository.findByMemberIdForUpdate(memberId))
        .willReturn(Optional.of(permanent));

    // when
    suspensionService.releaseIfExpired(memberId, Instant.now());

    // then
    verify(suspensionRepository, never()).deleteByMemberId(anyLong());
    Mockito.verifyNoInteractions(eventPublisher);
  }

  @Test
  @DisplayName("releaseIfExpired(): 이미 해제됐으면 아무것도 하지 않는다")
  void doNothingWhenAlreadyReleased() {
    // given
    var memberId = 33L;
    given(suspensionRepository.findByMemberIdForUpdate(memberId)).willReturn(Optional.empty());

    // when
    suspensionService.releaseIfExpired(memberId, Instant.now());

    // then
    verify(suspensionRepository, never()).deleteByMemberId(anyLong());
    Mockito.verifyNoInteractions(eventPublisher);
  }
}
