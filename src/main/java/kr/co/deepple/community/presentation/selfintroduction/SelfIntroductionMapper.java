package kr.co.deepple.community.presentation.selfintroduction;

import kr.co.deepple.community.presentation.selfintroduction.dto.SelfIntroductionSearchCondition;
import kr.co.deepple.community.presentation.selfintroduction.dto.SelfIntroductionSearchRequest;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.query.member.AgeConverter;

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
