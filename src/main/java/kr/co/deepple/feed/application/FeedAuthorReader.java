package kr.co.deepple.feed.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedAuthorType;
import kr.co.deepple.feed.domain.FeedErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedAuthorReader {

  private final FeedAuthorRepository feedAuthorRepository;

  public FeedAuthor getMember(long memberId) {
    return feedAuthorRepository
        .findByMemberId(memberId)
        .orElseThrow(
            () -> new DeeppleException(FeedErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
  }

  public FeedAuthor getOfficial() {
    return feedAuthorRepository
        .findFirstByAuthorType(FeedAuthorType.OFFICIAL)
        .orElseThrow(() -> new IllegalStateException("공식 계정 작성자가 없습니다."));
  }

  public FeedAuthor getAuthor(long authorId) {
    return feedAuthorRepository
        .findById(authorId)
        .orElseThrow(() -> new IllegalStateException("피드 작성자가 없습니다. authorId=" + authorId));
  }
}
