package kr.co.deepple.notification.application.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record NotificationReadRequest(@NotEmpty List<Long> notificationIds) {}
