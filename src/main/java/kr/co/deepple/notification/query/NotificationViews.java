package kr.co.deepple.notification.query;

import java.util.List;

public record NotificationViews(List<NotificationView> notifications, boolean hasMore) {}
