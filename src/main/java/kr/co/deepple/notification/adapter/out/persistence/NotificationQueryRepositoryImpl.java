package kr.co.deepple.notification.adapter.out.persistence;

import static kr.co.deepple.notification.domain.QNotification.notification;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.co.deepple.notification.application.dto.NotificationView;
import kr.co.deepple.notification.application.dto.QNotificationView;
import kr.co.deepple.notification.application.port.out.NotificationQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<NotificationView> findNotifications(long receiverId, Long lastId, int limit) {
    BooleanExpression condition =
        notification.receiverId.eq(receiverId).and(notification.deletedAt.isNull());

    if (lastId != null) {
      condition = condition.and(notification.id.lt(lastId));
    }

    return queryFactory
        .select(
            new QNotificationView(
                notification.id,
                notification.senderId,
                notification.receiverId,
                notification.type.stringValue(),
                notification.title,
                notification.body,
                notification.readAt.isNotNull(),
                notification.createdAt))
        .from(notification)
        .where(condition)
        .orderBy(notification.id.desc())
        .limit(limit)
        .fetch();
  }
}
