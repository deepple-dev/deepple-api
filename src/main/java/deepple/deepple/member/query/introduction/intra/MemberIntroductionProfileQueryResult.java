package deepple.deepple.member.query.introduction.intra;

import com.querydsl.core.annotations.QueryProjection;

import java.util.Set;

public record MemberIntroductionProfileQueryResult(
    long memberId,
    String profileImageUrl,
    Integer yearOfBirth,
    String nickname,
    String city,
    String district,
    Set<String> hobbies,
    String religion,
    String mbti,
    String likeLevel,
    boolean isIntroduced
) {
    @QueryProjection
    public MemberIntroductionProfileQueryResult {
    }
}
