package kr.co.deepple.datingexam.application;

import java.util.List;
import kr.co.deepple.datingexam.application.port.in.PersonalityTypeMemberFinder;
import kr.co.deepple.datingexam.application.port.out.PersonalityTypeMemberQueryRepository;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class PersonalityTypeMemberQueryService implements PersonalityTypeMemberFinder {

  private final PersonalityTypeMemberQueryRepository personalityTypeMemberQueryRepository;
  private final IntroducibleMemberFilter introducibleMemberFilter;

  @Override
  @Transactional(readOnly = true)
  public List<Long> findMemberIdsByDominantType(long memberId, AnswerPersonalityType type) {
    return introducibleMemberFilter.filter(
        memberId, personalityTypeMemberQueryRepository.findMemberIdsByDominantType(type));
  }
}
