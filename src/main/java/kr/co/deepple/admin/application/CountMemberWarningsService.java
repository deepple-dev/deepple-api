package kr.co.deepple.admin.application;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import kr.co.deepple.admin.application.port.in.CountMemberWarningsUseCase;
import kr.co.deepple.admin.application.port.out.WarningRepository;
import kr.co.deepple.admin.application.port.out.WarningRepository.MemberWarningCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CountMemberWarningsService implements CountMemberWarningsUseCase {

  private final WarningRepository warningRepository;

  @Override
  @Transactional(readOnly = true)
  public Map<Long, Integer> countMemberWarnings(Collection<Long> memberIds) {
    if (memberIds.isEmpty()) {
      return Map.of();
    }
    return warningRepository.countByMemberIds(memberIds).stream()
        .collect(
            Collectors.toMap(
                MemberWarningCount::getMemberId,
                warningCount -> Math.toIntExact(warningCount.getWarningCount())));
  }
}
