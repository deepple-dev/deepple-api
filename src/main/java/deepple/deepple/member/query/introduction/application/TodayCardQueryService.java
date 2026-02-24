package deepple.deepple.member.query.introduction.application;

import deepple.deepple.member.query.introduction.intra.IntroductionQueryRepository;
import deepple.deepple.member.query.introduction.intra.MemberIntroductionProfileQueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TodayCardQueryService {
    private final TodayCardMemberIdFetcher todayCardMemberIdFetcher;
    private final IntroductionQueryRepository introductionQueryRepository;

    @Transactional(readOnly = true)
    public Set<Long> findTodayCardMemberIds(long memberId) {
        return todayCardMemberIdFetcher.fetch(memberId, IntroductionCacheKeyPrefix.TODAY_CARD);
    }

    @Transactional(readOnly = true)
    public List<MemberIntroductionProfileView> findTodayCardIntroductions(long memberId, Set<Long> todayCardMemberIds) {
        List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
            introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
                todayCardMemberIds);
        return MemberIntroductionProfileViewMapper.mapWithDefaultTag(memberIntroductionProfileQueryResults);
    }
}
