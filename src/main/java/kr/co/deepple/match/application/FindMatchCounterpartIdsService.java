package kr.co.deepple.match.application;

import java.util.HashSet;
import java.util.Set;
import kr.co.deepple.match.application.port.in.FindMatchCounterpartIdsUseCase;
import kr.co.deepple.match.application.port.out.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindMatchCounterpartIdsService implements FindMatchCounterpartIdsUseCase {

  private final MatchRepository matchRepository;

  @Override
  @Transactional(readOnly = true)
  public Set<Long> findMatchCounterpartIds(long memberId) {
    Set<Long> memberIds = new HashSet<>(matchRepository.findRequesterIdsByResponderId(memberId));
    memberIds.addAll(matchRepository.findResponderIdsByRequesterId(memberId));
    return memberIds;
  }
}
