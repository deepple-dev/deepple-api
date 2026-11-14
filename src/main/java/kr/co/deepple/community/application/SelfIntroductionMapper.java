package kr.co.deepple.community.application;

import kr.co.deepple.community.application.dto.SelfIntroductionSearchCondition;
import kr.co.deepple.community.application.dto.SelfIntroductionSearchRequest;
import kr.co.deepple.member.domain.AgeConverter;
import kr.co.deepple.member.domain.Gender;

public class SelfIntroductionMapper {

  public static SelfIntroductionSearchCondition toSelfIntroductionSearchCondition(
      SelfIntroductionSearchRequest selfIntroductionSearchRequest) {
    return new SelfIntroductionSearchCondition(
        selfIntroductionSearchRequest.preferredCities(),
        AgeConverter.toYearOfBirth(selfIntroductionSearchRequest.toAge()),
        AgeConverter.toYearOfBirth(selfIntroductionSearchRequest.fromAge()),
        Gender.from(selfIntroductionSearchRequest.gender()));
  }
}
