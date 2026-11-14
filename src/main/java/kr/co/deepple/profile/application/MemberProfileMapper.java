package kr.co.deepple.profile.application;

import java.util.List;
import java.util.Objects;
import kr.co.deepple.match.application.port.in.MatchSummary;
import kr.co.deepple.member.application.port.in.MemberDetail;
import kr.co.deepple.member.application.port.in.MemberProfileImage;
import kr.co.deepple.profile.application.dto.BasicInfo;
import kr.co.deepple.profile.application.dto.ContactInfo;
import kr.co.deepple.profile.application.dto.MatchInfo;
import kr.co.deepple.profile.application.dto.MemberInfo;
import kr.co.deepple.profile.application.dto.ProfileImageInfo;
import kr.co.deepple.profile.application.dto.ProfileInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberProfileMapper {

  private static final String MATCHED = "MATCHED";
  private static final String PHONE_NUMBER = "PHONE_NUMBER";
  private static final String KAKAO = "KAKAO";

  public static BasicInfo toBasicInfo(MemberDetail member) {
    return new BasicInfo(
        member.nickname(),
        member.gender(),
        member.kakaoId(),
        member.age(),
        member.height(),
        member.phoneNumber());
  }

  public static ProfileInfo toProfileInfo(MemberDetail member) {
    return new ProfileInfo(
        member.job(),
        member.highestEducation(),
        member.city(),
        member.district(),
        member.mbti(),
        member.smokingStatus(),
        member.drinkingStatus(),
        member.religion(),
        member.hobbies());
  }

  public static MemberInfo toMemberInfo(
      MemberDetail member, List<MemberProfileImage> profileImages, String likeLevel) {
    String primaryImageUrl =
        profileImages.stream()
            .filter(MemberProfileImage::primary)
            .map(MemberProfileImage::imageUrl)
            .findFirst()
            .orElse(null);
    List<ProfileImageInfo> additionalProfileImages =
        profileImages.stream()
            .filter(profileImage -> !profileImage.primary())
            .map(
                profileImage -> new ProfileImageInfo(profileImage.imageUrl(), profileImage.order()))
            .toList();
    return new MemberInfo(
        member.memberId(),
        member.nickname(),
        primaryImageUrl,
        member.age(),
        member.gender(),
        member.height(),
        member.job(),
        member.hobbies(),
        member.mbti(),
        member.city(),
        member.district(),
        member.smokingStatus(),
        member.drinkingStatus(),
        member.highestEducation(),
        member.religion(),
        likeLevel,
        member.lastAccessedAt(),
        additionalProfileImages);
  }

  public static MatchInfo toMatchInfo(MatchSummary match) {
    return new MatchInfo(
        match.matchId(),
        match.requesterId(),
        match.responderId(),
        match.requestMessage(),
        match.responseMessage(),
        match.status(),
        match.requesterContactType(),
        match.responderContactType());
  }

  public static ContactInfo toContactInfo(MemberDetail member, MatchInfo matchInfo) {
    if (matchInfo == null || !MATCHED.equals(matchInfo.matchStatus())) {
      return null;
    }
    String contactType =
        Objects.equals(member.memberId(), matchInfo.requesterId())
            ? matchInfo.requesterContactType()
            : matchInfo.responderContactType();
    if (PHONE_NUMBER.equals(contactType)) {
      return new ContactInfo(contactType, member.phoneNumber());
    }
    if (KAKAO.equals(contactType)) {
      return new ContactInfo(contactType, member.kakaoId());
    }
    throw new IllegalStateException(
        "매칭의 연락 수단을 알 수 없습니다. matchId=" + matchInfo.matchId() + ", contactType=" + contactType);
  }
}
