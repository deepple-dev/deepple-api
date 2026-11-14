package kr.co.deepple.member.fixture;

import kr.co.deepple.member.application.port.in.MemberSummary;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.MemberProfile;
import kr.co.deepple.member.domain.Region;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberSummaryFixture {

  public static MemberSummary from(Member member) {
    MemberProfile profile = member.getProfile();
    Region region = profile == null ? null : profile.getRegion();
    return new MemberSummary(
        member.getId(),
        profile == null || profile.getNickname() == null ? null : profile.getNickname().getValue(),
        profile == null || profile.getGender() == null ? null : profile.getGender().name(),
        profile == null || profile.getYearOfBirth() == null
            ? null
            : profile.getYearOfBirth().getAge(),
        region == null || region.getCity() == null ? null : region.getCity().name(),
        region == null || region.getDistrict() == null ? null : region.getDistrict().name(),
        member.getGrade() == null ? null : member.getGrade().name(),
        member.getActivityStatus() == null ? null : member.getActivityStatus().name(),
        member.isVip(),
        member.hasSubmittedDatingExam());
  }
}
