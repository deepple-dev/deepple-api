package kr.co.deepple.datingexam.application;

import java.util.List;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.port.in.PersonalityTypeMemberFinder;
import kr.co.deepple.datingexam.application.port.in.ValidatePersonalityTypeUseCase;
import kr.co.deepple.datingexam.application.port.out.PersonalityTypeMemberQueryRepository;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.datingexam.domain.DatingExamErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class PersonalityTypeMemberQueryService
    implements PersonalityTypeMemberFinder, ValidatePersonalityTypeUseCase {

  private final PersonalityTypeMemberQueryRepository personalityTypeMemberQueryRepository;
  private final IntroducibleMemberFilter introducibleMemberFilter;

  @Override
  @Transactional(readOnly = true)
  public List<Long> findMemberIdsByDominantType(long memberId, String personalityType) {
    AnswerPersonalityType type = toPersonalityType(personalityType);
    return introducibleMemberFilter.filter(
        memberId, personalityTypeMemberQueryRepository.findMemberIdsByDominantType(type));
  }

  @Override
  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public void validate(String personalityType) {
    toPersonalityType(personalityType);
  }

  private static AnswerPersonalityType toPersonalityType(String personalityType) {
    try {
      return AnswerPersonalityType.valueOf(personalityType);
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new DeeppleException(
          DatingExamErrorCode.INVALID_PERSONALITY_TYPE, "personalityType=" + personalityType, e);
    }
  }
}
