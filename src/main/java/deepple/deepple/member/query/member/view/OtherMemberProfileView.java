package deepple.deepple.member.query.member.view;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;
import java.util.Set;

public record OtherMemberProfileView(
    BasicMemberInfo basicMemberInfo,
    MatchInfo matchInfo,
    ContactView contactView,
    ProfileExchangeInfo profileExchangeInfo,
    IntroductionInfo introductionInfo
) {
    @QueryProjection
    public OtherMemberProfileView(
        Long id, String nickname, String profileImageUrl,
        Integer yearOfBirth,
        String gender,
        Integer height,
        String job,
        Set<String> hobbies,
        String mbti,
        String city,
        String district,
        String smokingStatus,
        String drinkingStatus,
        String highestEducation,
        String religion,
        String like,
        LocalDateTime lastAccessedAt,
        Long matchId,
        Long requesterId,
        Long responderId,
        String requestMessage,
        String responseMessage,
        String matchStatus,
        String requesterContactType,
        String responderContactType,
        String phoneNumber,
        String kakaoId,
        Long profileExchangeId,
        Long profileExchangeRequesterId,
        Long profileExchangeResponderId,
        String profileExchangeStatus,
        String introductionType
    ) {
        this(
            new BasicMemberInfo(id, nickname, profileImageUrl, yearOfBirth, gender, height, job, hobbies, mbti, city,
                district, smokingStatus, drinkingStatus, highestEducation, religion, like, lastAccessedAt),
            matchId == null ? null
                : new MatchInfo(matchId, requesterId, responderId, requestMessage, responseMessage, matchStatus,
                    requesterContactType, responderContactType),
            new ContactView(phoneNumber, kakaoId),
            profileExchangeId == null ? null
                : new ProfileExchangeInfo(profileExchangeId, profileExchangeRequesterId, profileExchangeResponderId,
                    profileExchangeStatus),
            introductionType == null ? null
                : new IntroductionInfo(introductionType)
        );
    }
}
