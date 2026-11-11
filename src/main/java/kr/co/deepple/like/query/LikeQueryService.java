package kr.co.deepple.like.query;

import kr.co.deepple.like.presentation.LikeViews;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LikeQueryService {
    private static final int CLIENT_PAGE_SIZE = 12;
    private final LikeQueryRepository likeQueryRepository;

    public LikeViews findSentLikes(long senderId, Long lastLikeId) {
        var rawLikes = likeQueryRepository.findSentLikes(senderId, lastLikeId);
        boolean hasMore = rawLikes.size() > CLIENT_PAGE_SIZE;

        var likes = rawLikes.stream()
            .limit(CLIENT_PAGE_SIZE)
            .map(this::toLikeView)
            .toList();

        return new LikeViews(likes, hasMore);
    }

    public LikeViews findReceivedLikes(long receiverId, Long lastLikeId) {
        var rawLikes = likeQueryRepository.findReceivedLikes(receiverId, lastLikeId);
        boolean hasMore = rawLikes.size() > CLIENT_PAGE_SIZE;

        var likes = rawLikes.stream()
            .limit(CLIENT_PAGE_SIZE)
            .map(this::toLikeView)
            .toList();

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
            raw.createdAt()
        );
    }
}
