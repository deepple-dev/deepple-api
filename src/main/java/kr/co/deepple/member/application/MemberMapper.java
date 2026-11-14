package kr.co.deepple.member.application;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import kr.co.deepple.match.command.domain.match.MatchContactType;
import kr.co.deepple.match.command.domain.match.MatchStatus;
import kr.co.deepple.member.application.dto.BasicMemberInfo;
import kr.co.deepple.member.application.dto.ContactInfo;
import kr.co.deepple.member.application.dto.ContactView;
import kr.co.deepple.member.application.dto.MatchInfo;
import kr.co.deepple.member.application.dto.MemberInfo;
import kr.co.deepple.member.application.dto.MemberMyProfileResponse;
import kr.co.deepple.member.application.dto.MemberProfileUpdateRequest;
import kr.co.deepple.member.application.dto.MemberProfileView;
import kr.co.deepple.member.application.dto.ProfileImageInfo;
import kr.co.deepple.member.application.dto.ProfileImageView;
import kr.co.deepple.member.domain.AgeConverter;
import kr.co.deepple.member.domain.District;
import kr.co.deepple.member.domain.DrinkingStatus;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.HighestEducation;
import kr.co.deepple.member.domain.Hobby;
import kr.co.deepple.member.domain.Job;
import kr.co.deepple.member.domain.Mbti;
import kr.co.deepple.member.domain.MemberProfile;
import kr.co.deepple.member.domain.Nickname;
import kr.co.deepple.member.domain.Region;
import kr.co.deepple.member.domain.Religion;
import kr.co.deepple.member.domain.SmokingStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMapper {

  public static MemberProfile toMemberProfile(
      MemberProfileUpdateRequest memberProfileUpdateRequest) {
    return MemberProfile.builder()
        .yearOfBirth(memberProfileUpdateRequest.yearOfBirth())
        .height(memberProfileUpdateRequest.height())
        .nickname(Nickname.from(memberProfileUpdateRequest.nickname()))
        .gender(Gender.from(memberProfileUpdateRequest.gender()))
        .mbti(Mbti.from(memberProfileUpdateRequest.mbti()))
        .region(Region.from(District.from(memberProfileUpdateRequest.district())))
        .smokingStatus(SmokingStatus.from(memberProfileUpdateRequest.smokingStatus()))
        .drinkingStatus(DrinkingStatus.from(memberProfileUpdateRequest.drinkingStatus()))
        .religion(Religion.from(memberProfileUpdateRequest.religion()))
        .highestEducation(HighestEducation.from(memberProfileUpdateRequest.highestEducation()))
        .job(Job.from(memberProfileUpdateRequest.job()))
        .hobbies(
            memberProfileUpdateRequest.hobbies() != null
                ? memberProfileUpdateRequest.hobbies().stream()
                    .map(Hobby::from)
                    .collect(Collectors.toSet())
                : null)
        .build();
  }

  public static MemberInfo toBasicInfo(
      BasicMemberInfo basicMemberInfo, List<ProfileImageView> profileImages) {
    List<ProfileImageInfo> additionalProfileImages =
        profileImages.stream()
            .filter(profileImage -> !profileImage.isPrimary())
            .map(profileImage -> new ProfileImageInfo(profileImage.url(), profileImage.order()))
            .toList();
    return new MemberInfo(
        basicMemberInfo.id(),
        basicMemberInfo.nickname(),
        basicMemberInfo.profileImageUrl(),
        AgeConverter.toAge(basicMemberInfo.yearOfBirth()),
        basicMemberInfo.gender(),
        basicMemberInfo.height(),
        basicMemberInfo.job(),
        basicMemberInfo.hobbies(),
        basicMemberInfo.mbti(),
        basicMemberInfo.city(),
        basicMemberInfo.district(),
        basicMemberInfo.smokingStatus(),
        basicMemberInfo.drinkingStatus(),
        basicMemberInfo.highestEducation(),
        basicMemberInfo.religion(),
        basicMemberInfo.like(),
        basicMemberInfo.lastAccessedAt(),
        additionalProfileImages);
  }

  public static MemberMyProfileResponse toMemberMyProfileResponse(MemberProfileView view) {
    return new MemberMyProfileResponse(
        view.nickname(),
        AgeConverter.toAge(view.yearOfBirth()),
        view.gender(),
        view.height(),
        view.job(),
        view.hobbies(),
        view.mbti(),
        view.city(),
        view.district(),
        view.smokingStatus(),
        view.drinkingStatus(),
        view.highestEducation(),
        view.religion());
  }

  public static ContactInfo toContactInfo(
      ContactView contactView, MatchInfo matchInfo, long targetMemberId) {
    if (contactView == null || matchInfo == null) {
      return null;
    }
    if (matchInfo.matchStatus() == null
        || MatchStatus.valueOf(matchInfo.matchStatus()) != MatchStatus.MATCHED) {
      return null;
    }
    String contactType =
        Objects.equals(targetMemberId, matchInfo.requesterId())
            ? matchInfo.requesterContactType()
            : matchInfo.responderContactType();
    String contact =
        switch (MatchContactType.valueOf(contactType)) {
          case PHONE_NUMBER -> contactView.phoneNumber();
          case KAKAO -> contactView.kakaoId();
        };
    return new ContactInfo(contactType, contact);
  }
}
