package kr.co.deepple.member.command.application.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.event.MemberLoggedInEvent;
import kr.co.deepple.member.event.MemberRegisteredEvent;
import kr.co.deepple.member.event.MemberSummaryChangedEvent;
import kr.co.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class MemberAccountSyncServiceTest {

  private static final long MEMBER_ID = 42L;
  private static final Instant OCCURRED_AT = Instant.parse("2026-11-13T00:00:00Z");
  private static final LocalDateTime ACCESSED_AT =
      LocalDateTime.ofInstant(OCCURRED_AT, ZoneId.systemDefault());

  @Mock private MemberCommandRepository memberCommandRepository;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private MemberAccountSyncService memberAccountSyncService;

  @Test
  @DisplayName("계정 번호를 회원 번호로 써서 회원을 만들고 가입 이벤트와 요약 변경 이벤트를 발행한다.")
  void registerMemberWithAccountId() {
    when(memberCommandRepository.saveAndFlush(any(Member.class))).then(returnsFirstArg());

    memberAccountSyncService.register(MEMBER_ID, "01012345678");

    ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
    verify(memberCommandRepository).saveAndFlush(memberCaptor.capture());
    Member member = memberCaptor.getValue();
    assertThat(member.getId()).isEqualTo(MEMBER_ID);
    assertThat(member.getPhoneNumber()).isEqualTo("01012345678");
    assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.INITIAL);

    ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
    verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getAllValues().get(0))
        .isInstanceOfSatisfying(
            MemberRegisteredEvent.class,
            event -> assertThat(event.memberId()).isEqualTo(MEMBER_ID));
    assertThat(eventCaptor.getAllValues().get(1)).isInstanceOf(MemberSummaryChangedEvent.class);
  }

  @Test
  @DisplayName("활동 중인 회원이 로그인하면 로그인 이벤트를 발행하고 마지막 접속 시각을 갱신한다.")
  void publishLoggedInEventForActiveMember() {
    when(memberCommandRepository.findById(MEMBER_ID))
        .thenReturn(Optional.of(MemberFixture.builder().id(MEMBER_ID).build()));

    memberAccountSyncService.recordLogin(MEMBER_ID, OCCURRED_AT);

    verify(eventPublisher).publishEvent(any(MemberLoggedInEvent.class));
    verify(memberCommandRepository).updateLastAccessedAt(MEMBER_ID, ACCESSED_AT);
  }

  @Test
  @DisplayName("휴면 회원이 로그인하면 로그인 이벤트는 발행하지 않고 마지막 접속 시각만 갱신한다.")
  void skipLoggedInEventForDormantMember() {
    when(memberCommandRepository.findById(MEMBER_ID))
        .thenReturn(
            Optional.of(
                MemberFixture.builder()
                    .id(MEMBER_ID)
                    .activityStatus(ActivityStatus.DORMANT)
                    .build()));

    memberAccountSyncService.recordLogin(MEMBER_ID, OCCURRED_AT);

    verify(eventPublisher, never()).publishEvent(any(Object.class));
    verify(memberCommandRepository).updateLastAccessedAt(MEMBER_ID, ACCESSED_AT);
  }

  @Test
  @DisplayName("토큰을 재발급하면 마지막 접속 시각을 갱신한다.")
  void updateLastAccessedAtOnRefresh() {
    memberAccountSyncService.recordAccess(MEMBER_ID, OCCURRED_AT);

    verify(memberCommandRepository).updateLastAccessedAt(MEMBER_ID, ACCESSED_AT);
  }
}
