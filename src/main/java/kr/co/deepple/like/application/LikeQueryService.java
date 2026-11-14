package kr.co.deepple.like.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import kr.co.deepple.block.application.port.in.FindBlockedMemberIdsUseCase;
import kr.co.deepple.like.adapter.out.persistence.LikeQueryRepository;
import kr.co.deepple.like.application.dto.LikeView;
import kr.co.deepple.like.application.dto.LikeViews;
import kr.co.deepple.like.application.dto.RawLikeView;
import kr.co.deepple.member.application.port.in.FindMemberPreviewsUseCase;
import kr.co.deepple.member.application.port.in.MemberPreview;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeQueryService {

  private static final int CLIENT_PAGE_SIZE = 12;
  private static final int MAX_FETCH_ATTEMPTS = 5;

  private final LikeQueryRepository likeQueryRepository;
  private final FindBlockedMemberIdsUseCase findBlockedMemberIdsUseCase;
  private final FindMemberPreviewsUseCase findMemberPreviewsUseCase;

  public LikeViews findSentLikes(long senderId, Long lastLikeId) {
    Set<Long> blockedIds = findBlockedMemberIdsUseCase.findBlockedMemberIds(senderId);
    return collectLikes(
        lastLikeId, cursor -> likeQueryRepository.findSentLikes(senderId, cursor, blockedIds));
  }

  public LikeViews findReceivedLikes(long receiverId, Long lastLikeId) {
    Set<Long> blockedIds = findBlockedMemberIdsUseCase.findBlockedMemberIds(receiverId);
    return collectLikes(
        lastLikeId,
        cursor -> likeQueryRepository.findReceivedLikes(receiverId, cursor, blockedIds));
  }

  private LikeViews collectLikes(Long lastLikeId, Function<Long, List<RawLikeView>> fetchAfter) {
    List<LikeView> likes = new ArrayList<>();
    Long cursor = lastLikeId;
    for (int attempt = 0; attempt < MAX_FETCH_ATTEMPTS; attempt++) {
      List<RawLikeView> rawLikes = fetchAfter.apply(cursor);
      Map<Long, MemberPreview> previews =
          findMemberPreviewsUseCase.findMemberPreviews(
              rawLikes.stream().map(RawLikeView::opponentId).toList());
      for (RawLikeView raw : rawLikes) {
        if (likes.size() == CLIENT_PAGE_SIZE) {
          return new LikeViews(likes, true);
        }
        cursor = raw.likeId();
        MemberPreview opponent = previews.get(raw.opponentId());
        if (opponent != null) {
          likes.add(toLikeView(raw, opponent));
        }
      }
      if (rawLikes.size() <= CLIENT_PAGE_SIZE) {
        return new LikeViews(likes, false);
      }
    }
    return new LikeViews(likes, true);
  }

  private LikeView toLikeView(RawLikeView raw, MemberPreview opponent) {
    return new LikeView(
        raw.likeId(),
        raw.opponentId(),
        opponent.profileImageUrl(),
        opponent.nickname(),
        opponent.city(),
        opponent.yearOfBirth() == null ? null : LocalDate.now().getYear() - opponent.yearOfBirth(),
        raw.isMutualLike(),
        raw.createdAt());
  }
}
