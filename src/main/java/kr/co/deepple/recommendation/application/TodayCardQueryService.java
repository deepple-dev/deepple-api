package kr.co.deepple.recommendation.application;

import java.util.List;
import java.util.Set;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodayCardQueryService {

  private final TodayCardMemberIdFetcher todayCardMemberIdFetcher;
  private final MemberIntroductionProfileViewAssembler memberIntroductionProfileViewAssembler;

  @Transactional(readOnly = true)
  public Set<Long> findTodayCardMemberIds(long memberId) {
    return todayCardMemberIdFetcher.fetch(memberId, IntroductionCacheKeyPrefix.TODAY_CARD);
  }

  @Transactional(readOnly = true)
  public List<MemberIntroductionProfileView> findTodayCardIntroductions(
      long memberId, Set<Long> todayCardMemberIds) {
    return memberIntroductionProfileViewAssembler.assemble(memberId, todayCardMemberIds);
  }
}
