package kr.co.deepple.community.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
import kr.co.deepple.like.domain.LikeLevel;
import kr.co.deepple.member.domain.AgeConverter;

public record SelfIntroductionView(
    MemberBasicInfo memberBasicInfo,
    @Schema(implementation = LikeLevel.class) String like,
    String title,
    String content,
    String imageUrl,
    String profileExchangeStatus,
    LocalDateTime createdAt) {
  @QueryProjection
  public SelfIntroductionView(
      Long memberId,
      String nickname,
      Integer yearOfBirth,
      String city,
      String district,
      String mbti,
      Set<String> hobbies,
      String gender,
      String personalityType,
      String like,
      String title,
      String content,
      String imageUrl,
      String profileExchangeStatus,
      LocalDateTime createdAt) {
    this(
        new MemberBasicInfo(
            memberId,
            nickname,
            AgeConverter.toAge(yearOfBirth),
            city,
            district,
            mbti,
            hobbies,
            gender,
            personalityType),
        like,
        title,
        content,
        imageUrl,
        profileExchangeStatus,
        createdAt);
  }
}
