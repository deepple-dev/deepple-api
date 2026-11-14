package kr.co.deepple.like.application;

import java.time.LocalDate;
import java.util.List;
import kr.co.deepple.like.adapter.out.persistence.LikeQueryRepository;
import kr.co.deepple.like.application.dto.LikeView;
import kr.co.deepple.like.application.dto.LikeViews;
import kr.co.deepple.like.application.dto.RawLikeView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeQueryService {

  private static final int CLIENT_PAGE_SIZE = 12;
  private final LikeQueryRepository likeQueryRepository;

  public LikeViews findSentLikes(long senderId, Long lastLikeId) {
    List<RawLikeView> rawLikes = likeQueryRepository.findSentLikes(senderId, lastLikeId);
    boolean hasMore = rawLikes.size() > CLIENT_PAGE_SIZE;

    List<LikeView> likes = rawLikes.stream().limit(CLIENT_PAGE_SIZE).map(this::toLikeView).toList();

    return new LikeViews(likes, hasMore);
  }

  public LikeViews findReceivedLikes(long receiverId, Long lastLikeId) {
    List<RawLikeView> rawLikes = likeQueryRepository.findReceivedLikes(receiverId, lastLikeId);
    boolean hasMore = rawLikes.size() > CLIENT_PAGE_SIZE;

    List<LikeView> likes = rawLikes.stream().limit(CLIENT_PAGE_SIZE).map(this::toLikeView).toList();

    return new LikeViews(likes, hasMore);
  }

  private LikeView toLikeView(RawLikeView raw) {
    return new LikeView(
        raw.likeId(),
        raw.opponentId(),
        raw.profileImageUrl(),
        raw.nickname(),
        raw.city(),
        LocalDate.now().getYear() - raw.yearOfBirth(),
        raw.isMutualLike(),
        raw.createdAt());
  }
}
