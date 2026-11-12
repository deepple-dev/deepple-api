package kr.co.deepple.match.query;

import kr.co.deepple.match.presentation.dto.MatchViews;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchQueryService {
    private static final int CLIENT_PAGE_SIZE = 12;
    private final MatchQueryRepository matchQueryRepository;

    public MatchViews getSentMatches(long requesterId, Long lastMatchId) {
        List<MatchView> matches = matchQueryRepository.findSentMatches(requesterId, lastMatchId);
        return paginateMatches(matches);
    }

    public MatchViews getReceivedMatches(long responderId, Long lastMatchId) {
        List<MatchView> matches = matchQueryRepository.findReceiveMatches(responderId, lastMatchId);
        return paginateMatches(matches);
    }

    private MatchViews paginateMatches(List<MatchView> matches) {
        boolean hasMore = matches.size() > CLIENT_PAGE_SIZE;
        if (hasMore) {
            matches = matches.subList(0, CLIENT_PAGE_SIZE);
        }
        return new MatchViews(matches, hasMore);
    }
}
