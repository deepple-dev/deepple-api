package kr.co.deepple.notification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.co.deepple.notification.domain.NotificationDestination;
import kr.co.deepple.notification.domain.NotificationType;
import kr.co.deepple.notification.domain.SenderType;

public record NotificationView(
    long notificationId,
    @Schema(implementation = SenderType.class) String senderType,
    long senderId,
    @Schema(description = "보낸 회원의 대표 사진. 회원이 보낸 알림이 아니거나 사진이 없으면 null이다")
        String senderProfileImageUrl,
    long receiverId,
    @Schema(implementation = NotificationType.class) String notificationType,
    String title,
    String body,
    boolean isRead,
    LocalDateTime createdAt,
    @Schema(
            implementation = NotificationDestination.class,
            description =
                "눌렀을 때 이동할 곳 - MESSAGE: 상대와의 메시지 상세, PROFILE: 상대 프로필, PROFILE_EDIT: 내 프로필 수정, "
                    + "INTERVIEW: 내 인터뷰, HEART_HISTORY: 하트 사용 내역, NONE: 이동 없음")
        String destination,
    @Schema(description = "이동할 곳의 회원 ID(MESSAGE·PROFILE이면 상대 회원 ID). 그 밖에는 null이다")
        Long destinationId) {}
