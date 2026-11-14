package kr.co.deepple.member.application;

import java.util.Set;
import kr.co.deepple.member.adapter.out.persistence.MemberQueryRepository;
import kr.co.deepple.member.application.port.in.MemberSearchCondition;
import kr.co.deepple.member.application.port.in.SearchIntroducibleMemberIdsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchIntroducibleMemberIdsService implements SearchIntroducibleMemberIdsUseCase {

  private final MemberQueryRepository memberQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public Set<Long> searchMemberIds(MemberSearchCondition condition, long limit) {
    return memberQueryRepository.searchIntroducibleIds(condition, limit);
  }
}
