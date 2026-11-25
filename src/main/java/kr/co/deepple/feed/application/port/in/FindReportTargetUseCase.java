package kr.co.deepple.feed.application.port.in;

import java.util.Optional;

public interface FindReportTargetUseCase {

  Optional<Long> findFeedAuthorMemberId(long reporterId, long feedId);

  Optional<Long> findCommentAuthorMemberId(long reporterId, long commentId);
}
