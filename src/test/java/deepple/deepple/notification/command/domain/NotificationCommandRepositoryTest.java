package deepple.deepple.notification.command.domain;

import deepple.deepple.common.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static deepple.deepple.notification.command.domain.NotificationType.LIKE;
import static deepple.deepple.notification.command.domain.SenderType.SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class NotificationCommandRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private NotificationCommandRepository repository;

    @Test
    @DisplayName("DB의 CURRENT_TIMESTAMP로 저장한 읽음 시각이 앱의 현재 시각과 같은 시간대다.")
    void markAllAsReadWithAppTimeZone() {
        // given
        Notification notification = Notification.create(SYSTEM, 1L, 2L, LIKE, "title", "body");
        em.persist(notification);
        em.flush();
        em.clear();

        // when
        repository.markAllAsReadByIdIn(List.of(notification.getId()), 2L);

        // then
        LocalDateTime readAt = em.find(Notification.class, notification.getId()).getReadAt();
        assertThat(readAt).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.MINUTES));
    }
}
