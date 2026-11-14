package kr.co.deepple.notification.application.port.out;

import java.util.List;
import kr.co.deepple.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  @Modifying(clearAutomatically = true)
  @Query(
      "UPDATE Notification n SET n.deletedAt = CURRENT_TIMESTAMP WHERE n.id IN :ids AND n.receiverId = :receiverId AND n.deletedAt IS NULL")
  void deleteAllByIdIn(@Param("ids") List<Long> ids, @Param("receiverId") long receiverId);

  @Modifying(clearAutomatically = true)
  @Query(
      "UPDATE Notification n SET n.readAt = CURRENT_TIMESTAMP WHERE n.id IN :ids AND n.receiverId = :receiverId AND n.readAt IS NULL")
  void markAllAsReadByIdIn(@Param("ids") List<Long> ids, @Param("receiverId") long receiverId);
}
