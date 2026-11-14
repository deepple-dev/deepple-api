package kr.co.deepple.member.application;

import java.util.Set;
import kr.co.deepple.member.application.port.in.MemberIdSearchCondition;
import kr.co.deepple.member.application.port.in.SearchMemberIdsUseCase;
import kr.co.deepple.member.application.port.out.MemberQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchMemberIdsService implements SearchMemberIdsUseCase {

  private final MemberQueryRepository memberQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public Set<Long> searchMemberIds(MemberIdSearchCondition condition) {
    if (condition.isEmpty()) {
      throw new IllegalArgumentException("회원 검색 조건이 하나는 있어야 합니다.");
    }
    return memberQueryRepository.searchIds(condition);
  }
}
