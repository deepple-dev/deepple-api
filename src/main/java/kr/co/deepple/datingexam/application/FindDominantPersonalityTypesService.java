package kr.co.deepple.datingexam.application;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import kr.co.deepple.datingexam.application.port.in.DominantPersonalityType;
import kr.co.deepple.datingexam.application.port.in.FindDominantPersonalityTypesUseCase;
import kr.co.deepple.datingexam.application.port.out.DatingExamSubmitResultRepository;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.datingexam.domain.DatingExamSubmitResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindDominantPersonalityTypesService implements FindDominantPersonalityTypesUseCase {

  private final DatingExamSubmitResultRepository datingExamSubmitResultRepository;

  @Override
  @Transactional(readOnly = true)
  public Map<Long, String> findDominantPersonalityTypes(Collection<Long> memberIds) {
    if (memberIds.isEmpty()) {
      return Map.of();
    }
    return datingExamSubmitResultRepository.findByMemberIdIn(memberIds).stream()
        .collect(
            Collectors.toMap(
                DatingExamSubmitResult::getMemberId,
                result -> result.getDominantPersonalityType().name()));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<DominantPersonalityType> findDominantPersonalityType(long memberId) {
    return datingExamSubmitResultRepository
        .findByMemberId(memberId)
        .map(DatingExamSubmitResult::getDominantPersonalityType)
        .map(FindDominantPersonalityTypesService::toDominantPersonalityType);
  }

  private static DominantPersonalityType toDominantPersonalityType(AnswerPersonalityType type) {
    return new DominantPersonalityType(type.name(), type.getDescription());
  }
}
