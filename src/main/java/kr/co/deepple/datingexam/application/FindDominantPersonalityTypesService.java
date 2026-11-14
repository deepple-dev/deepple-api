package kr.co.deepple.datingexam.application;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import kr.co.deepple.datingexam.application.port.in.FindDominantPersonalityTypesUseCase;
import kr.co.deepple.datingexam.application.port.out.DatingExamSubmitResultRepository;
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
}
