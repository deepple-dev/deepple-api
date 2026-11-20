package kr.co.deepple.feed.application;

import java.time.Instant;
import java.time.LocalDateTime;
import kr.co.deepple.feed.application.port.in.SyncFeedAuthorProfileCommand;
import kr.co.deepple.feed.application.port.in.SyncFeedAuthorUseCase;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncFeedAuthorService implements SyncFeedAuthorUseCase {

  private final FeedAuthorRepository feedAuthorRepository;

  @Override
  @Transactional
  public void syncProfile(SyncFeedAuthorProfileCommand command) {
    getAuthorForUpdate(command.memberId())
        .updateProfile(
            command.nickname(),
            command.gender(),
            command.yearOfBirth(),
            command.city(),
            command.mbti(),
            command.occurredAt());
  }

  @Override
  @Transactional
  public void syncProfileImage(long memberId, String profileImageUrl, Instant occurredAt) {
    getAuthorForUpdate(memberId).updateProfileImage(profileImageUrl, occurredAt);
  }

  @Override
  @Transactional
  public void syncPersonalityType(long memberId, String personalityType, Instant occurredAt) {
    getAuthorForUpdate(memberId).updatePersonalityType(personalityType, occurredAt);
  }

  @Override
  @Transactional
  public void syncActivityStatus(long memberId, String activityStatus, Instant occurredAt) {
    getAuthorForUpdate(memberId).updateActivityStatus(activityStatus, occurredAt);
  }

  @Override
  @Transactional
  public void syncGrade(long memberId, String grade, Instant occurredAt) {
    getAuthorForUpdate(memberId).updateGrade(grade, occurredAt);
  }

  private FeedAuthor getAuthorForUpdate(long memberId) {
    feedAuthorRepository.insertIfAbsent(memberId, LocalDateTime.now());
    return feedAuthorRepository
        .findByMemberIdForUpdate(memberId)
        .orElseThrow(() -> new IllegalStateException("피드 작성자 사본이 없습니다. memberId=" + memberId));
  }
}
