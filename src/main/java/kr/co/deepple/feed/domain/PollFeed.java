package kr.co.deepple.feed.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.BatchSize;

@Entity
@DiscriminatorValue("POLL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PollFeed extends UserFeed {

  private static final int MIN_OPTION_COUNT = 2;
  private static final int MAX_OPTION_COUNT = 4;
  private static final int MAX_TEXT_LENGTH = 20;
  private static final String LINE_BREAK = "\n";

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "feed_id", nullable = false, updatable = false)
  @OrderBy("displayOrder")
  @BatchSize(size = 100)
  private List<FeedPollOption> options = new ArrayList<>();

  private PollFeed(
      long authorId,
      String grade,
      String question,
      TextPosition textPosition,
      Long backgroundId,
      FeedImage image,
      List<String> options,
      String idempotencyKey) {
    super(authorId, grade, question, textPosition, backgroundId, image, idempotencyKey);
    IntStream.range(0, options.size())
        .mapToObj(index -> FeedPollOption.of(options.get(index), index))
        .forEach(this.options::add);
  }

  public static PollFeed create(
      long authorId,
      @NonNull String grade,
      @NonNull String question,
      @NonNull TextPosition textPosition,
      Long backgroundId,
      FeedImage image,
      @NonNull List<String> options,
      @NonNull String idempotencyKey) {
    validateCanvas(backgroundId, image);
    validateQuestion(question);
    validateOptions(options);
    return new PollFeed(
        authorId, grade, question, textPosition, backgroundId, image, options, idempotencyKey);
  }

  private static void validateQuestion(String question) {
    if (!isValidPollText(question)) {
      throw new DeeppleException(FeedErrorCode.INVALID_FEED_CONTENT, "length=" + question.length());
    }
  }

  private static void validateOptions(List<String> options) {
    if (options.size() < MIN_OPTION_COUNT || options.size() > MAX_OPTION_COUNT) {
      throw new DeeppleException(FeedErrorCode.INVALID_POLL_OPTIONS, "count=" + options.size());
    }
    if (!options.stream().allMatch(PollFeed::isValidPollText)) {
      throw new DeeppleException(FeedErrorCode.INVALID_POLL_OPTIONS, "count=" + options.size());
    }
  }

  private static boolean isValidPollText(String text) {
    return text != null
        && !text.isBlank()
        && text.length() <= MAX_TEXT_LENGTH
        && !text.contains(LINE_BREAK);
  }

  @Override
  public FeedType getType() {
    return FeedType.POLL;
  }
}
