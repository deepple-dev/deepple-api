package deepple.deepple.notification.query;

import deepple.deepple.common.support.RepositoryTestSupport;
import deepple.deepple.notification.command.domain.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static deepple.deepple.notification.command.domain.NotificationType.LIKE;
import static deepple.deepple.notification.command.domain.SenderType.SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;

class NotificationQueryRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private NotificationQueryRepository repository;

    @Test
    @DisplayName("findNotifications(): 첫 페이지 조회 (lastId = null)")
    void findNotificationsFirstPage() {
        // given
        var unread = Notification.create(SYSTEM, 1L, 2L, LIKE, "unread", "b1");
        var read = Notification.create(SYSTEM, 2L, 2L, LIKE, "read", "b2");
        read.markAsRead();

        em.persist(unread);
        em.persist(read);
        em.flush();
        em.clear();

        // when
        var results = repository.findNotifications(2L, null, 20);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("title")
            .containsExactlyInAnyOrder("unread", "read");
        assertThat(results).extracting("isRead")
            .containsExactlyInAnyOrder(false, true);
    }

    @Test
    @DisplayName("findNotifications(): 다음 페이지 조회 (lastId != null)")
    void findNotificationsNextPage() {
        // given
        var n1 = Notification.create(SYSTEM, 1L, 3L, LIKE, "n1", "b1");
        var n2 = Notification.create(SYSTEM, 1L, 3L, LIKE, "n2", "b2");
        var n3 = Notification.create(SYSTEM, 1L, 3L, LIKE, "n3", "b3");

        em.persist(n1);
        em.persist(n2);
        em.persist(n3);
        em.flush();
        em.clear();

        // when
        Long lastId = n3.getId();
        var results = repository.findNotifications(3L, lastId, 20);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("title")
            .containsExactly("n2", "n1");
    }

    @Test
    @DisplayName("findNotifications(): 삭제된 알림은 조회되지 않음")
    void findNotificationsExcludeDeleted() {
        // given
        var active = Notification.create(SYSTEM, 1L, 4L, LIKE, "active", "body");
        var deleted = Notification.create(SYSTEM, 1L, 4L, LIKE, "deleted", "body");
        deleted.delete();

        em.persist(active);
        em.persist(deleted);
        em.flush();
        em.clear();

        // when
        var results = repository.findNotifications(4L, null, 20);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().title()).isEqualTo("active");
    }
}
