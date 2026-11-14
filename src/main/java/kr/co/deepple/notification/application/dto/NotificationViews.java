package kr.co.deepple.notification.application.dto;

import java.util.List;

public record NotificationViews(List<NotificationView> notifications, boolean hasMore) {}
