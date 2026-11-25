package kr.co.deepple.feed.application;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.dto.AdminFeedCreateRequest;
import kr.co.deepple.feed.application.dto.AdminFeedUpdateRequest;
import kr.co.deepple.feed.application.dto.FeedCreateRequest;
import kr.co.deepple.feed.application.dto.FeedTextPosition;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedDeletedEvent;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedImage;
import kr.co.deepple.feed.domain.FeedPublishedEvent;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.domain.FeedType;
import kr.co.deepple.feed.domain.FeedUpdatedEvent;
import kr.co.deepple.feed.domain.OfficialFeed;
import kr.co.deepple.feed.domain.PollFeed;
import kr.co.deepple.feed.domain.TextFeed;
import kr.co.deepple.feed.domain.TextPosition;
import kr.co.deepple.feed.domain.UserFeed;
import kr.co.deepple.image.application.port.in.FindImageUseCase;
import kr.co.deepple.image.application.port.in.ImageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

  @Transactional
  public long createOfficial(
      FeedAuthor officialAuthor,
      long adminId,
      String idempotencyKey,
      AdminFeedCreateRequest request) {
    OfficialFeed feed =
        OfficialFeed.create(
            officialAuthor.getId(),
            adminId,
            request.title(),
            request.content(),
            findAdminImages(request.imageIds()),
            toTargetGrades(request.targetGrades()),
            request.isPublished(),
            request.isPinned(),
            idempotencyKey);
    if (feed.isPublished()) {
      validateImagesProcessed(feed);
    }
    long feedId = feedRepository.save(feed).getId();
    if (feed.isPublished()) {
      eventPublisher.publishEvent(new FeedPublishedEvent(feedId));
    }
    log.atInfo()
        .addKeyValue("feedId", feedId)
        .addKeyValue("status", feed.getStatus())
        .log("관리자 피드 등록");
    return feedId;
  }

  @Transactional
  public OfficialFeed updateOfficial(long feedId, AdminFeedUpdateRequest request) {
    OfficialFeed feed = getOfficialFeedForUpdate(feedId);
    if (request.title() != null) {
      feed.changeTitle(request.title());
    }
    if (request.content() != null) {
      feed.changeContent(request.content());
    }
    if (request.imageIds() != null) {
      feed.changeImages(findAdminImages(request.imageIds()));
      if (feed.isPublished()) {
        validateImagesProcessed(feed);
      }
    }
    if (request.targetGrades() != null) {
      feed.changeTargetGrades(toTargetGrades(request.targetGrades()));
    }
    if (request.isPinned() != null) {
      feed.changePinned(request.isPinned());
    }
    eventPublisher.publishEvent(new FeedUpdatedEvent(feedId));
    log.atInfo().addKeyValue("feedId", feedId).log("관리자 피드 수정");
    return feed;
  }

  @Transactional
  public UserFeed changeUserFeedStatus(long feedId, FeedStatus status) {
    UserFeed feed = getUserFeedForUpdate(feedId);
    if (status == FeedStatus.PUBLISHED) {
      validateImagesProcessed(feed);
    }
    feed.changeStatus(status);
    publishStatusChanged(feed, status);
    return feed;
  }

  @Transactional
  public OfficialFeed changeOfficialFeedStatus(long feedId, FeedStatus status) {
    OfficialFeed feed = getOfficialFeedForUpdate(feedId);
    if (status == FeedStatus.PUBLISHED) {
      validateImagesProcessed(feed);
    }
    feed.changeStatus(status);
    publishStatusChanged(feed, status);
    return feed;
  }

  @Transactional
  public void deleteUserFeed(long feedId) {
    delete(getUserFeedForUpdate(feedId));
  }

  @Transactional
  public void deleteOfficialFeed(long feedId) {
    delete(getOfficialFeedForUpdate(feedId));
  }

  private void publishStatusChanged(Feed feed, FeedStatus status) {
    eventPublisher.publishEvent(new FeedUpdatedEvent(feed.getId()));
    log.atInfo()
        .addKeyValue("feedId", feed.getId())
        .addKeyValue("feedType", feed.getType())
        .addKeyValue("status", status)
        .log("관리자 피드 상태 변경");
  }

  private void delete(Feed feed) {
    feed.delete();
    eventPublisher.publishEvent(new FeedDeletedEvent(feed.getId()));
    log.atInfo()
        .addKeyValue("feedId", feed.getId())
        .addKeyValue("feedType", feed.getType())
        .log("관리자 피드 삭제");
  }

  private UserFeed getUserFeedForUpdate(long feedId) {
    return feedRepository
        .findUserFeedByIdForUpdate(feedId)
        .orElseThrow(() -> new DeeppleException(FeedErrorCode.FEED_NOT_FOUND, "feedId=" + feedId));
  }

  private OfficialFeed getOfficialFeedForUpdate(long feedId) {
    return feedRepository
        .findOfficialFeedByIdForUpdate(feedId)
        .orElseThrow(() -> new DeeppleException(FeedErrorCode.FEED_NOT_FOUND, "feedId=" + feedId));
  }

  private void validateImagesProcessed(Feed feed) {
    List<Long> imageIds = feed.getImageIds();
    if (!findImageUseCase.findCompletedImageIds(imageIds).containsAll(imageIds)) {
      throw new DeeppleException(
          FeedErrorCode.FEED_IMAGE_NOT_PROCESSED,
          "feedId=" + feed.getId() + ", imageIds=" + imageIds);
    }
  }

  private List<FeedImage> findAdminImages(List<Long> imageIds) {
    if (new HashSet<>(imageIds).size() != imageIds.size()) {
      throw new DeeppleException(FeedErrorCode.INVALID_FEED_IMAGES, "imageIds=" + imageIds);
    }
    return IntStream.range(0, imageIds.size())
        .mapToObj(index -> {
          ImageInfo image = findImageUseCase.getAdminImage(imageIds.get(index));
          return FeedImage.of(image.imageId(), image.imageKey(), image.thumbnailKey(), index);
        })
        .toList();
  }

  private Set<String> toTargetGrades(List<String> targetGrades) {
    return targetGrades == null ? Set.of() : Set.copyOf(targetGrades);
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
