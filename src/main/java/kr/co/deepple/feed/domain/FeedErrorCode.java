package kr.co.deepple.feed.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedErrorCode implements ErrorCode {
  FEED_NOT_FOUND(ErrorType.NOT_FOUND, "피드를 찾을 수 없습니다."),
  MEMBER_NOT_FOUND(ErrorType.NOT_FOUND, "회원을 찾을 수 없습니다."),
  FEED_BACKGROUND_NOT_FOUND(ErrorType.NOT_FOUND, "배경을 찾을 수 없습니다."),
  POLL_OPTION_NOT_FOUND(ErrorType.NOT_FOUND, "투표 선택지를 찾을 수 없습니다."),
  FEED_VOTE_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 투표한 피드입니다."),
  SELF_FEED_VOTE(ErrorType.INVALID_ARGUMENT, "자신의 투표에는 투표할 수 없습니다."),
  INVALID_FEED_TYPE(ErrorType.INVALID_ARGUMENT, "등록할 수 없는 피드 종류입니다."),
  INVALID_FEED_CANVAS(ErrorType.INVALID_ARGUMENT, "배경과 사진 중 하나만 골라야 합니다."),
  INVALID_FEED_CONTENT(ErrorType.INVALID_ARGUMENT, "피드 내용이 올바르지 않습니다."),
  INVALID_POLL_OPTIONS(ErrorType.INVALID_ARGUMENT, "투표 선택지가 올바르지 않습니다."),
  INVALID_FEED_IMAGES(ErrorType.INVALID_ARGUMENT, "피드 이미지가 올바르지 않습니다."),
  INVALID_FEED_CURSOR(ErrorType.INVALID_ARGUMENT, "커서가 올바르지 않습니다.");

  private final ErrorType type;
  private final String message;
}
