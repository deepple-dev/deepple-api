package kr.co.deepple.notification.query;

import static kr.co.deepple.notification.command.domain.NotificationType.LIKE;
import static kr.co.deepple.notification.command.domain.SenderType.SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.notification.fixture.NotificationFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class NotificationQueryRepositoryTest extends RepositoryTestSupport {

  @Autowired private TestEntityManager em;

  @Autowired private NotificationQueryRepository repository;

  @Test
  @DisplayName("findNotifications(): 첫 페이지 조회 (lastId = null)")
  void findNotificationsFirstPage() {
    // given
    var unread =
        NotificationFixture.builder()
            .senderType(SYSTEM)
            .senderId(1L)
            .receiverId(2L)
            .type(LIKE)
            .title("unread")
            .body("b1")
            .build();
    var read =
        NotificationFixture.builder()
            .senderType(SYSTEM)
            .senderId(2L)
            .receiverId(2L)
            .type(LIKE)
            .title("read")
            .body("b2")
            .readAt(LocalDateTime.now())
            .build();

    em.persist(unread);
    em.persist(read);
    em.flush();
    em.clear();

    // when
    var results = repository.findNotifications(2L, null, 20);

    // then
    assertThat(results).hasSize(2);
    assertThat(results).extracting("title").containsExactlyInAnyOrder("unread", "read");
    assertThat(results).extracting("isRead").containsExactlyInAnyOrder(false, true);
  }

  @Test
  @DisplayName("findNotifications(): 다음 페이지 조회 (lastId != null)")
  void findNotificationsNextPage() {
    // given
    var n1 =
        NotificationFixture.builder()
            .senderType(SYSTEM)
            .senderId(1L)
            .receiverId(3L)
            .type(LIKE)
            .title("n1")
            .body("b1")
            .build();
    var n2 =
        NotificationFixture.builder()
            .senderType(SYSTEM)
            .senderId(1L)
            .receiverId(3L)
            .type(LIKE)
            .title("n2")
            .body("b2")
            .build();
    var n3 =
        NotificationFixture.builder()
            .senderType(SYSTEM)
            .senderId(1L)
            .receiverId(3L)
            .type(LIKE)
            .title("n3")
            .body("b3")
            .build();

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
    assertThat(results).extracting("title").containsExactly("n2", "n1");
  }

  @Test
  @DisplayName("findNotifications(): 삭제된 알림은 조회되지 않음")
  void findNotificationsExcludeDeleted() {
    // given
    var active =
        NotificationFixture.builder()
            .senderType(SYSTEM)
            .senderId(1L)
            .receiverId(4L)
            .type(LIKE)
            .title("active")
            .body("body")
            .build();
    var deleted =
        NotificationFixture.builder()
            .senderType(SYSTEM)
            .senderId(1L)
            .receiverId(4L)
            .type(LIKE)
            .title("deleted")
            .body("body")
            .build();
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
