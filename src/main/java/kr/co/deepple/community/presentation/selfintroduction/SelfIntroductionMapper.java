package kr.co.deepple.community.presentation.selfintroduction;

import kr.co.deepple.community.presentation.selfintroduction.dto.SelfIntroductionSearchCondition;
import kr.co.deepple.community.presentation.selfintroduction.dto.SelfIntroductionSearchRequest;
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
