package kr.co.deepple.notification.application.port.in;

public record MemberNotificationSetting(long memberId, boolean enabledGlobally) {}
