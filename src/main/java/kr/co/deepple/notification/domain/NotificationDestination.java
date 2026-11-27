package kr.co.deepple.notification.domain;

public enum NotificationDestination {
  MESSAGE,
  PROFILE,
  PROFILE_EDIT,
  INTERVIEW,
  HEART_HISTORY,
  NONE;

  public static NotificationDestination of(NotificationType type) {
    return switch (type) {
      case MATCH_REQUEST,
          MATCH_ACCEPT,
          MATCH_REJECT,
          PROFILE_EXCHANGE_REQUEST,
          PROFILE_EXCHANGE_ACCEPT,
          PROFILE_EXCHANGE_REJECT ->
          MESSAGE;
      case LIKE -> PROFILE;
      case PROFILE_IMAGE_CHANGE_REQUEST, INAPPROPRIATE_PROFILE, INAPPROPRIATE_PROFILE_IMAGE ->
          PROFILE_EDIT;
      case INTERVIEW_WRITE_REQUEST, INAPPROPRIATE_INTERVIEW -> INTERVIEW;
      case HEART_GRANTED -> HEART_HISTORY;
      case INAPPROPRIATE_SELF_INTRODUCTION,
          REPORT,
          SCREENING_APPROVED,
          SCREENING_REJECTED,
          INACTIVITY_REMINDER ->
          NONE;
    };
  }

  public Long destinationId(SenderType senderType, long senderId) {
    return senderType == SenderType.MEMBER && (this == MESSAGE || this == PROFILE)
        ? senderId
        : null;
  }
}
