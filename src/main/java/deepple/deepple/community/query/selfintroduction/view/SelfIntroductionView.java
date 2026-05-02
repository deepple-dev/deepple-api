package deepple.deepple.community.query.selfintroduction.view;

import com.querydsl.core.annotations.QueryProjection;
import deepple.deepple.like.command.domain.LikeLevel;
import deepple.deepple.member.query.member.AgeConverter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

public record SelfIntroductionView(
    MemberBasicInfo memberBasicInfo,
    @Schema(implementation = LikeLevel.class)
    String like,
    String title,
    String content,
    String imageUrl,
    String profileExchangeStatus,
    LocalDateTime createdAt
) {
    @QueryProjection
    public SelfIntroductionView(Long memberId,
        String nickname,
        Integer yearOfBirth,
        String profileImageUrl,
        String city,
        String district,
        String mbti,
        Set<String> hobbies,
        String gender,
        String like,
        String title,
        String content,
        String imageUrl,
        String profileExchangeStatus,
        LocalDateTime createdAt
    ) {
        this(new MemberBasicInfo(memberId, nickname, AgeConverter.toAge(yearOfBirth), profileImageUrl, city, district,
            mbti, hobbies, gender), like, title, content, imageUrl, profileExchangeStatus, createdAt);
    }
}
