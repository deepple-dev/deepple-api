package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.AdminFeedCommentView;

public interface FindAdminFeedCommentUseCase {

  AdminFeedCommentView getComment(long commentId);
}
