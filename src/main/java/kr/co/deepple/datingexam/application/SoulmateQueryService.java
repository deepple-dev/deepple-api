package kr.co.deepple.datingexam.application;

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

  @Override
  @Transactional(readOnly = true)
  public Set<Long> findSoulmateIds(long memberId) {
    return soulmateQueryRepository.findSameAnswerMemberIds(memberId);
  }
}
