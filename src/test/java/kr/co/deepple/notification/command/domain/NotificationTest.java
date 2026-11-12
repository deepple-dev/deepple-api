package kr.co.deepple.notification.command.domain;

import kr.co.deepple.notification.fixture.NotificationFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static kr.co.deepple.notification.command.domain.NotificationStatus.*;
import static kr.co.deepple.notification.command.domain.NotificationType.LIKE;
import static kr.co.deepple.notification.command.domain.SenderType.MEMBER;
import static kr.co.deepple.notification.command.domain.SenderType.SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    @DisplayName("정상 데이터로 생성 시 필드가 모두 설정되고 readAt은 null로 설정됨")
    void createWithValidArguments() {
        // given
        var senderType = MEMBER;
        long senderId = 100L;
        long receiverId = 200L;
        var type = LIKE;
        String title = "테스트 제목";
        String body = "테스트 본문";

        // when
        var notification = Notification.create(senderType, senderId, receiverId, type, title, body);

        // then
        assertThat(notification).isNotNull();
        assertThat(notification.getSenderType()).isEqualTo(senderType);
        assertThat(notification.getSenderId()).isEqualTo(senderId);
        assertThat(notification.getReceiverId()).isEqualTo(receiverId);
        assertThat(notification.getType()).isEqualTo(type);
        assertThat(notification.getTitle()).isEqualTo(title);
        assertThat(notification.getBody()).isEqualTo(body);
        assertThat(notification.getReadAt()).isNull();
        assertThat(notification.getStatus()).isEqualTo(CREATED);
        assertThat(notification.isRead()).isFalse();
    }

    @Test
    @DisplayName("markAsRead 호출 후 isRead가 true로 변경되고 readAt이 설정됨")
    void markAsReadChangesState() {
        // given
        var notification = NotificationFixture.builder()
            .senderType(SYSTEM)
            .type(LIKE)
            .build();

        // when
        notification.markAsRead();

        // then
        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getReadAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("markAsRead를 여러 번 호출해도 readAt은 최초 한 번만 설정됨")
    void markAsReadIdempotent() {
        // given
        var notification = NotificationFixture.builder()
            .senderType(SYSTEM)
            .type(LIKE)
            .build();

        // when
        notification.markAsRead();
        LocalDateTime firstTime = notification.getReadAt();
        notification.markAsRead();

        // then
        assertThat(notification.getReadAt()).isEqualTo(firstTime);
    }

    @Test
    @DisplayName("markAsSent 호출 시 status가 SENT로 변경됨")
    void markAsSentSetsStatus() {
        // given
        var notification = NotificationFixture.builder()
            .type(LIKE)
            .build();

        // when
        notification.markAsSent();

        // then
        assertThat(notification.getStatus()).isEqualTo(SENT);
    }

    @Test
    @DisplayName("createFailed로 FAILED_UNSUPPORTED_CHANNEL 생성 시 status가 FAILED_UNSUPPORTED_CHANNEL로 설정됨")
    void createFailedUnsupportedChannelSetsStatus() {
        // when
        var notification = Notification.createFailed(MEMBER, 1L, 2L, LIKE, "제목", "내용", FAILED_UNSUPPORTED_CHANNEL);

        // then
        assertThat(notification.getStatus()).isEqualTo(FAILED_UNSUPPORTED_CHANNEL);
    }

    @Test
    @DisplayName("createFailed로 FAILED_EXCEPTION 생성 시 status가 FAILED_EXCEPTION로 설정됨")
    void createFailedExceptionSetsStatus() {
        // when
        var notification = Notification.createFailed(MEMBER, 1L, 2L, LIKE, "제목", "내용", FAILED_EXCEPTION);

        // then
        assertThat(notification.getStatus()).isEqualTo(FAILED_EXCEPTION);
    }

    @Test
    @DisplayName("createFailed로 REJECTED_BY_PREFERENCE 생성 시 status가 REJECTED_BY_PREFERENCE로 설정됨")
    void createFailedRejectedByPreferenceSetsStatus() {
        // when
        var notification = Notification.createFailed(MEMBER, 1L, 2L, LIKE, "제목", "내용", REJECTED_BY_PREFERENCE);

        // then
        assertThat(notification.getStatus()).isEqualTo(REJECTED_BY_PREFERENCE);
    }
}
