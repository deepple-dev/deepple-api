package kr.co.deepple.notification.query;

import static kr.co.deepple.notification.command.domain.QNotificationPreference.notificationPreference;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import kr.co.deepple.notification.command.domain.NotificationPreference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationPreferenceQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<NotificationPreferenceView> findByMemberId(long memberId) {
    NotificationPreference preference =
        queryFactory
            .selectFrom(notificationPreference)
            .where(notificationPreference.memberId.eq(memberId))
            .fetchOne();

    return Optional.ofNullable(preference).map(this::toView);
  }

  private NotificationPreferenceView toView(NotificationPreference preference) {
    return new NotificationPreferenceView(
        preference.getMemberId(),
        preference.isEnabledGlobally(),
        preference.getNotificationPreferences().entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue)));
  }
}
