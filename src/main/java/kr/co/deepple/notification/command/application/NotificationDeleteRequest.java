package kr.co.deepple.notification.command.application;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record NotificationDeleteRequest(@NotEmpty List<Long> notificationIds) {}
