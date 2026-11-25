package kr.co.deepple.report.application.port.out.spi;

import java.util.Optional;

public interface ReportTargetFinder {

  Optional<Long> findFeedAuthorMemberId(long reporterId, long feedId);

  Optional<Long> findCommentAuthorMemberId(long reporterId, long commentId);
}
