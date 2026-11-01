package deepple.deepple.member.presentation.member;

import deepple.deepple.match.command.domain.match.MatchContactType;
import deepple.deepple.match.command.domain.match.MatchStatus;
import deepple.deepple.member.command.domain.member.*;
import deepple.deepple.member.command.domain.member.vo.MemberProfile;
import deepple.deepple.member.command.domain.member.vo.Nickname;
import deepple.deepple.member.command.domain.member.vo.Region;
import deepple.deepple.member.presentation.member.dto.*;
import deepple.deepple.member.query.member.AgeConverter;
import deepple.deepple.member.query.member.view.BasicMemberInfo;
import deepple.deepple.member.query.member.view.ContactView;
import deepple.deepple.member.query.member.view.MatchInfo;
import deepple.deepple.member.query.member.view.MemberProfileView;
import deepple.deepple.member.query.profileimage.view.ProfileImageView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMapper {

    public static MemberProfile toMemberProfile(MemberProfileUpdateRequest memberProfileUpdateRequest) {
        return MemberProfile.builder()
            .yearOfBirth(memberProfileUpdateRequest.yearOfBirth())
            .height(memberProfileUpdateRequest.height())
            .nickname(Nickname.from(memberProfileUpdateRequest.nickname()))
            .gender(Gender.from(memberProfileUpdateRequest.gender()))
            .mbti(Mbti.from(memberProfileUpdateRequest.mbti()))
            .region(Region.of(District.from(memberProfileUpdateRequest.district())))
            .smokingStatus(SmokingStatus.from(memberProfileUpdateRequest.smokingStatus()))
            .drinkingStatus(DrinkingStatus.from(memberProfileUpdateRequest.drinkingStatus()))
            .religion(Religion.from(memberProfileUpdateRequest.religion()))
            .highestEducation(HighestEducation.from(memberProfileUpdateRequest.highestEducation()))
            .job(Job.from(memberProfileUpdateRequest.job()))
            .hobbies(memberProfileUpdateRequest.hobbies() != null ? memberProfileUpdateRequest.hobbies()
                .stream()
                .map(Hobby::from)
                .collect(Collectors.toSet()) : null)
            .build();
    }

    public static MemberInfo toBasicInfo(BasicMemberInfo basicMemberInfo, List<ProfileImageView> profileImages) {
        List<ProfileImageInfo> additionalProfileImages = profileImages.stream()
            .filter(profileImage -> !profileImage.isPrimary())
            .map(profileImage -> new ProfileImageInfo(profileImage.url(), profileImage.order()))
            .toList();
        return new MemberInfo(basicMemberInfo.id(), basicMemberInfo.nickname(), basicMemberInfo.profileImageUrl(),
            AgeConverter.toAge(basicMemberInfo.yearOfBirth()), basicMemberInfo.gender(), basicMemberInfo.height(),
            basicMemberInfo.job(), basicMemberInfo.hobbies(), basicMemberInfo.mbti(), basicMemberInfo.city(),
            basicMemberInfo.district(),
            basicMemberInfo.smokingStatus(), basicMemberInfo.drinkingStatus(), basicMemberInfo.highestEducation(),
            basicMemberInfo.religion(), basicMemberInfo.like(), basicMemberInfo.lastAccessedAt(),
            additionalProfileImages);
    }

    public static MemberMyProfileResponse toMemberMyProfileResponse(MemberProfileView view) {
        return new MemberMyProfileResponse(view.nickname(), AgeConverter.toAge(view.yearOfBirth()), view.gender(),
            view.height(),
            view.job(), view.hobbies(), view.mbti(), view.city(), view.district(), view.smokingStatus(),
            view.drinkingStatus(), view.highestEducation(), view.religion());
    }

    public static ContactInfo toContactInfo(ContactView contactView, MatchInfo matchInfo, Long targetMemberId) {
        if (contactView == null || matchInfo == null) {
            return null;
        }
        if (matchInfo.matchStatus() == null || MatchStatus.valueOf(matchInfo.matchStatus()) != MatchStatus.MATCHED) {
            return null;
        }
        String contactType = targetMemberId.equals(matchInfo.requesterId()) ? matchInfo.requesterContactType()
            : matchInfo.responderContactType();
        String contact = switch (MatchContactType.valueOf(contactType)) {
            case PHONE_NUMBER -> contactView.phoneNumber();
            case KAKAO -> contactView.kakaoId();
        };
        return new ContactInfo(contactType, contact);
    }
}
