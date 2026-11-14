package kr.co.deepple.datingexam.application;

import java.util.HashSet;
import java.util.Set;
import kr.co.deepple.datingexam.application.port.in.SoulmateFinder;
import kr.co.deepple.datingexam.application.port.out.SoulmateQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class SoulmateQueryService implements SoulmateFinder {

  private final SoulmateQueryRepository soulmateQueryRepository;
  private final IntroducibleMemberFilter introducibleMemberFilter;

  @Override
  @Transactional(readOnly = true)
  public Set<Long> findSoulmateIds(long memberId) {
    return new HashSet<>(
        introducibleMemberFilter.filter(
            memberId, soulmateQueryRepository.findSameAnswerMemberIds(memberId)));
  }
}
