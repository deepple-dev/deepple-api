package kr.co.deepple.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.co.deepple.member.application.port.in.FindMemberPreviewsUseCase;
import kr.co.deepple.member.application.port.in.MemberPreview;
import kr.co.deepple.notification.application.dto.NotificationRow;
import kr.co.deepple.notification.application.dto.NotificationView;
import kr.co.deepple.notification.application.dto.NotificationViews;
import kr.co.deepple.notification.application.port.out.NotificationQueryRepository;
import kr.co.deepple.notification.domain.NotificationType;
import kr.co.deepple.notification.domain.SenderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {

  private static final long RECEIVER_ID = 1L;
  private static final long SENDER_ID = 2L;
  private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 11, 27, 10, 0);

  @Mock private NotificationQueryRepository notificationQueryRepository;
  @Mock private FindMemberPreviewsUseCase findMemberPreviewsUseCase;

  @InjectMocks private NotificationQueryService notificationQueryService;

  @Test
  @DisplayName("회원이 보낸 알림은 보낸 회원의 대표 사진과 메시지 상세 이동 대상을 함께 응답한다.")
  void respondSenderImageAndDestinationForMemberNotification() {
    given(notificationQueryRepository.findNotifications(RECEIVER_ID, null, 21))
        .willReturn(
            List.of(row(10L, SenderType.MEMBER, SENDER_ID, NotificationType.MATCH_REQUEST)));
    given(findMemberPreviewsUseCase.findMemberPreviews(Set.of(SENDER_ID)))
        .willReturn(
            Map.of(
                SENDER_ID,
                new MemberPreview(SENDER_ID, "보낸이", "https://cdn/sender.webp", null, null, null)));

    NotificationViews views = notificationQueryService.findNotifications(RECEIVER_ID, null);

    NotificationView view = views.notifications().getFirst();
    assertThat(view.senderType()).isEqualTo("MEMBER");
    assertThat(view.senderProfileImageUrl()).isEqualTo("https://cdn/sender.webp");
    assertThat(view.destination()).isEqualTo("MESSAGE");
    assertThat(view.destinationId()).isEqualTo(SENDER_ID);
    assertThat(views.hasMore()).isFalse();
  }

  @Test
  @DisplayName("관리자·시스템 알림은 보낸 사람 사진을 찾지 않고 이동 대상 ID도 비운다.")
  void notLookUpSenderForSystemNotification() {
    given(notificationQueryRepository.findNotifications(RECEIVER_ID, null, 21))
        .willReturn(List.of(row(11L, SenderType.ADMIN, 99L, NotificationType.HEART_GRANTED)));

    NotificationView view =
        notificationQueryService.findNotifications(RECEIVER_ID, null).notifications().getFirst();

    assertThat(view.senderProfileImageUrl()).isNull();
    assertThat(view.destination()).isEqualTo("HEART_HISTORY");
    assertThat(view.destinationId()).isNull();
    verify(findMemberPreviewsUseCase, never()).findMemberPreviews(anyCollection());
  }

  @Test
  @DisplayName("다음 페이지가 있으면 잘라낸 20개의 회원 보낸이만 조회하고, 사진 없는 보낸이는 null로 응답한다.")
  void lookUpOnlyMemberSendersInPage() {
    List<NotificationRow> rows = new ArrayList<>();
    rows.add(row(1L, SenderType.ADMIN, 99L, NotificationType.SCREENING_APPROVED));
    for (long i = 2; i <= 21; i++) {
      rows.add(row(i, SenderType.MEMBER, 100L + i, NotificationType.LIKE));
    }
    given(notificationQueryRepository.findNotifications(RECEIVER_ID, null, 21)).willReturn(rows);
    Set<Long> expectedSenderIds = new HashSet<>();
    for (long i = 2; i <= 20; i++) {
      expectedSenderIds.add(100L + i);
    }
    given(findMemberPreviewsUseCase.findMemberPreviews(expectedSenderIds)).willReturn(Map.of());

    NotificationViews views = notificationQueryService.findNotifications(RECEIVER_ID, null);

    assertThat(views.hasMore()).isTrue();
    assertThat(views.notifications()).hasSize(20);
    assertThat(views.notifications().get(1).senderProfileImageUrl()).isNull();
    assertThat(views.notifications().get(1).destinationId()).isEqualTo(102L);
  }

  private static NotificationRow row(
      long id, SenderType senderType, long senderId, NotificationType type) {
    return new NotificationRow(
        id, senderType, senderId, RECEIVER_ID, type, "제목", "본문", false, CREATED_AT);
  }
}
