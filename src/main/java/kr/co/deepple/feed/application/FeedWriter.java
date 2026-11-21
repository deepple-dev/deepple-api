package kr.co.deepple.feed.application;

import java.util.Arrays;
import java.util.List;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.dto.FeedCreateRequest;
import kr.co.deepple.feed.application.dto.FeedTextPosition;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedImage;
import kr.co.deepple.feed.domain.FeedPublishedEvent;
import kr.co.deepple.feed.domain.FeedType;
import kr.co.deepple.feed.domain.PollFeed;
import kr.co.deepple.feed.domain.TextFeed;
import kr.co.deepple.feed.domain.TextPosition;
import kr.co.deepple.image.application.port.in.FindImageUseCase;
import kr.co.deepple.image.application.port.in.ImageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FeedWriter {

  private final FeedRepository feedRepository;
  private final FeedBackgroundReader feedBackgroundReader;
  private final FindImageUseCase findImageUseCase;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public long create(FeedAuthor author, String idempotencyKey, FeedCreateRequest request) {
    String grade = getGrade(author);
    validateBackground(request.backgroundId());
    FeedImage image = findImage(request.imageId(), author.getMemberId());
    Feed feed =
        switch (parseType(request.type())) {
          case TEXT -> createTextFeed(author, grade, image, idempotencyKey, request);
          case POLL -> createPollFeed(author, grade, image, idempotencyKey, request);
          case OFFICIAL ->
              throw new DeeppleException(FeedErrorCode.INVALID_FEED_TYPE, "type=" + request.type());
        };
    long feedId = feedRepository.save(feed).getId();
    if (feed.isPublished()) {
      eventPublisher.publishEvent(new FeedPublishedEvent(feedId));
    }
    return feedId;
  }

  private TextFeed createTextFeed(
      FeedAuthor author,
      String grade,
      FeedImage image,
      String idempotencyKey,
      FeedCreateRequest request) {
    rejectPollOptions(request.pollOptions(), author.getMemberId());
    return TextFeed.create(
        author.getId(),
        grade,
        request.content(),
        toTextPosition(request.textPosition()),
        request.backgroundId(),
        image,
        idempotencyKey);
  }

  private PollFeed createPollFeed(
      FeedAuthor author,
      String grade,
      FeedImage image,
      String idempotencyKey,
      FeedCreateRequest request) {
    return PollFeed.create(
        author.getId(),
        grade,
        request.content(),
        toTextPosition(request.textPosition()),
        request.backgroundId(),
        image,
        request.pollOptions() == null ? List.of() : request.pollOptions(),
        idempotencyKey);
  }

  private FeedType parseType(String type) {
    return Arrays.stream(FeedType.values())
        .filter(value -> value.name().equals(type))
        .findFirst()
        .orElseThrow(() -> new DeeppleException(FeedErrorCode.INVALID_FEED_TYPE, "type=" + type));
  }

  private void rejectPollOptions(List<String> pollOptions, long memberId) {
    if (pollOptions != null && !pollOptions.isEmpty()) {
      throw new DeeppleException(FeedErrorCode.INVALID_POLL_OPTIONS, "memberId=" + memberId);
    }
  }

  private String getGrade(FeedAuthor author) {
    String grade = author.getGrade();
    if (grade == null) {
      throw new IllegalStateException("작성자 등급이 없습니다. memberId=" + author.getMemberId());
    }
    return grade;
  }

  private void validateBackground(Long backgroundId) {
    if (backgroundId == null) {
      return;
    }
    boolean isVisible =
        feedBackgroundReader.readCatalog().visibleBackgrounds().stream()
            .anyMatch(background -> background.backgroundId() == backgroundId);
    if (!isVisible) {
      throw new DeeppleException(
          FeedErrorCode.FEED_BACKGROUND_NOT_FOUND, "backgroundId=" + backgroundId);
    }
  }

  private FeedImage findImage(Long imageId, long memberId) {
    if (imageId == null) {
      return null;
    }
    ImageInfo image = findImageUseCase.getMemberImage(imageId, memberId);
    return FeedImage.of(image.imageId(), image.imageKey(), image.thumbnailKey(), 0);
  }

  private TextPosition toTextPosition(FeedTextPosition position) {
    return new TextPosition(position.x(), position.y(), position.rotation(), position.scale());
  }
}
