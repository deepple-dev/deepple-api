package kr.co.deepple.recommendation.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecommendationErrorCode implements ErrorCode {
  MEMBER_IDEAL_NOT_FOUND(ErrorType.NOT_FOUND, "이상형 정보를 찾을 수 없습니다."),
  INVALID_AGE_RANGE(ErrorType.INVALID_ARGUMENT, "유효하지 않은 나이 범위입니다."),
  INVALID_IDEAL_CITY_COUNT(ErrorType.INVALID_ARGUMENT, "이상형 지역은 2개까지 선택할 수 있습니다."),
  SELF_MEMBER_INTRODUCTION(ErrorType.INVALID_ARGUMENT, "자기 자신을 소개받을 수 없습니다."),
  MEMBER_INTRODUCTION_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 소개받은 회원입니다."),
  INTRODUCED_MEMBER_NOT_ACTIVE(ErrorType.FAILED_PRECONDITION, "활동 중이 아닌 회원은 소개받을 수 없습니다."),
  INTRODUCED_MEMBER_BLOCKED(ErrorType.PERMISSION_DENIED, "차단 관계인 회원은 소개받을 수 없습니다."),
  PERSONALITY_INTRODUCTION_ALREADY_OPENED(ErrorType.FAILED_PRECONDITION, "오늘은 이미 다른 유형을 소개받았습니다."),
  INVALID_PERSONALITY_INTRODUCTION_UNLOCK(ErrorType.FAILED_PRECONDITION, "소개 목록에 없는 회원입니다."),
  DATING_EXAM_NOT_SUBMITTED(ErrorType.FAILED_PRECONDITION, "연애고사를 제출한 회원만 소울메이트를 소개받을 수 있습니다."),
  NOT_SOULMATE(ErrorType.INVALID_ARGUMENT, "소울메이트가 아닌 회원입니다."),
  NOT_SAME_ANSWER_MEMBER(ErrorType.INVALID_ARGUMENT, "같은 답안을 제출한 회원이 아닙니다.");

  private final ErrorType type;
  private final String message;
}
