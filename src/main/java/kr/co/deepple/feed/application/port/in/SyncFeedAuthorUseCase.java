package kr.co.deepple.feed.application.port.in;

import java.time.Instant;

public interface SyncFeedAuthorUseCase {

  void syncProfile(SyncFeedAuthorProfileCommand command);

  void syncProfileImage(long memberId, String profileImageUrl, Instant occurredAt);

  void syncPersonalityType(long memberId, String personalityType, Instant occurredAt);

  void syncActivityStatus(long memberId, String activityStatus, Instant occurredAt);

  void syncGrade(long memberId, String grade, Instant occurredAt);
}
