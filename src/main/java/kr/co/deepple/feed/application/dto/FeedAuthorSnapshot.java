package kr.co.deepple.feed.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedAuthorStatus;
import kr.co.deepple.feed.domain.FeedAuthorType;

public record FeedAuthorSnapshot(
    long authorId,
    FeedAuthorType authorType,
    Long memberId,
    String nickname,
    String personalityType,
    String profileImageUrl,
    Integer yearOfBirth,
    String gender,
    String city,
    String mbti,
    boolean isActive)
    implements FeedAuthorStatus {

  public static FeedAuthorSnapshot from(FeedAuthor author) {
    return new FeedAuthorSnapshot(
        author.getId(),
        author.getAuthorType(),
        author.getMemberId(),
        author.getNickname(),
        author.getPersonalityType(),
        author.getProfileImageUrl(),
        author.getYearOfBirth(),
        author.getGender(),
        author.getCity(),
        author.getMbti(),
        author.isActive());
  }

  @Override
  public Long getMemberId() {
    return memberId;
  }

  @Override
  @JsonIgnore
  public boolean isOfficial() {
    return authorType == FeedAuthorType.OFFICIAL;
  }
}
