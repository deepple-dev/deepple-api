package kr.co.deepple.notification.application;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.member.application.port.in.FindMemberPreviewsUseCase;
import kr.co.deepple.member.application.port.in.MemberPreview;
import kr.co.deepple.notification.application.dto.NotificationRow;
import kr.co.deepple.notification.application.dto.NotificationView;
import kr.co.deepple.notification.application.dto.NotificationViews;
import kr.co.deepple.notification.application.port.out.NotificationQueryRepository;
import kr.co.deepple.notification.domain.NotificationDestination;
import kr.co.deepple.notification.domain.SenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

  private static final int CLIENT_PAGE_SIZE = 20;
  private final NotificationQueryRepository notificationQueryRepository;
  private final FindMemberPreviewsUseCase findMemberPreviewsUseCase;

  public NotificationViews findNotifications(long receiverId, Long lastId) {
    List<NotificationRow> rows =
        notificationQueryRepository.findNotifications(receiverId, lastId, CLIENT_PAGE_SIZE + 1);
    List<NotificationRow> page = rows.stream().limit(CLIENT_PAGE_SIZE).toList();
    Map<Long, MemberPreview> senders = findMemberSenders(page);
    List<NotificationView> notifications =
        page.stream().map(row -> toView(row, senders.get(row.senderId()))).toList();
    return new NotificationViews(notifications, rows.size() > CLIENT_PAGE_SIZE);
  }

  private Map<Long, MemberPreview> findMemberSenders(List<NotificationRow> rows) {
    Set<Long> senderIds =
        rows.stream()
            .filter(NotificationQueryService::isSentByMember)
            .map(NotificationRow::senderId)
            .collect(Collectors.toSet());
    if (senderIds.isEmpty()) {
      return Map.of();
    }
    return findMemberPreviewsUseCase.findMemberPreviews(senderIds);
  }

  private static NotificationView toView(NotificationRow row, MemberPreview sender) {
    NotificationDestination destination = NotificationDestination.of(row.notificationType());
    return new NotificationView(
        row.notificationId(),
        row.senderType().name(),
        row.senderId(),
        isSentByMember(row) && sender != null ? sender.profileImageUrl() : null,
        row.receiverId(),
        row.notificationType().name(),
        row.title(),
        row.body(),
        row.isRead(),
        row.createdAt(),
        destination.name(),
        destination.destinationId(row.senderType(), row.senderId()));
  }

  private static boolean isSentByMember(NotificationRow row) {
    return row.senderType() == SenderType.MEMBER;
  }
}
