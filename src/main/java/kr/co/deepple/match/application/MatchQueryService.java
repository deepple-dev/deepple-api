package kr.co.deepple.match.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import kr.co.deepple.block.application.port.in.FindBlockedMemberIdsUseCase;
import kr.co.deepple.match.application.dto.MatchView;
import kr.co.deepple.match.application.dto.MatchViews;
import kr.co.deepple.match.application.dto.RawMatchView;
import kr.co.deepple.match.application.port.out.MatchQueryRepository;
import kr.co.deepple.member.application.port.in.FindMemberPreviewsUseCase;
import kr.co.deepple.member.application.port.in.MemberPreview;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchQueryService {

  private static final int CLIENT_PAGE_SIZE = 12;
  private static final int MAX_FETCH_ATTEMPTS = 5;

  private final MatchQueryRepository matchQueryRepository;
  private final FindBlockedMemberIdsUseCase findBlockedMemberIdsUseCase;
  private final FindMemberPreviewsUseCase findMemberPreviewsUseCase;

  public MatchViews getSentMatches(long requesterId, Long lastMatchId) {
    Set<Long> blockedIds = findBlockedMemberIdsUseCase.findBlockedMemberIds(requesterId);
    return collectMatches(
        lastMatchId,
        cursor -> matchQueryRepository.findSentMatches(requesterId, cursor, blockedIds));
  }

  public MatchViews getReceivedMatches(long responderId, Long lastMatchId) {
    Set<Long> blockedIds = findBlockedMemberIdsUseCase.findBlockedMemberIds(responderId);
    return collectMatches(
        lastMatchId,
        cursor -> matchQueryRepository.findReceiveMatches(responderId, cursor, blockedIds));
  }

  private MatchViews collectMatches(
      Long lastMatchId, Function<Long, List<RawMatchView>> fetchAfter) {
    List<MatchView> matches = new ArrayList<>();
    Long cursor = lastMatchId;
    for (int attempt = 0; attempt < MAX_FETCH_ATTEMPTS; attempt++) {
      List<RawMatchView> rawMatches = fetchAfter.apply(cursor);
      Map<Long, MemberPreview> previews =
          findMemberPreviewsUseCase.findMemberPreviews(
              rawMatches.stream().map(RawMatchView::opponentId).toList());
      for (RawMatchView raw : rawMatches) {
        if (matches.size() == CLIENT_PAGE_SIZE) {
          return new MatchViews(matches, true);
        }
        cursor = raw.matchId();
        MemberPreview opponent = previews.get(raw.opponentId());
        if (opponent != null) {
          matches.add(toMatchView(raw, opponent));
        }
      }
      if (rawMatches.size() <= CLIENT_PAGE_SIZE) {
        return new MatchViews(matches, false);
      }
    }
    return new MatchViews(matches, true);
  }

  private MatchView toMatchView(RawMatchView raw, MemberPreview opponent) {
    return new MatchView(
        raw.matchId(),
        raw.opponentId(),
        raw.opponentMessage(),
        opponent.nickname(),
        opponent.profileImageUrl(),
        opponent.city(),
        raw.myMessage(),
        raw.matchStatus(),
        raw.createdAt(),
        raw.readAt());
  }
}
