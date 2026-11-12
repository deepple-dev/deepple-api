package kr.co.deepple.community.command.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunityErrorCode implements ErrorCode {
  PROFILE_EXCHANGE_NOT_FOUND(ErrorType.NOT_FOUND, "프로필 교환을 찾을 수 없습니다."),
  PROFILE_EXCHANGE_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 프로필 교환을 신청한 회원입니다."),
  SELF_PROFILE_EXCHANGE(ErrorType.INVALID_ARGUMENT, "자기 자신에게 프로필 교환을 신청할 수 없습니다."),
  PROFILE_EXCHANGE_RESPONDER_INACTIVE(
      ErrorType.FAILED_PRECONDITION, "활동 중이 아닌 회원에게는 프로필 교환을 신청할 수 없습니다."),
  PROFILE_EXCHANGE_BLOCKED(ErrorType.PERMISSION_DENIED, "차단 관계인 회원에게는 프로필 교환을 신청할 수 없습니다."),
  PROFILE_EXCHANGE_ACCESS_DENIED(ErrorType.PERMISSION_DENIED, "프로필 교환 요청의 응답 대상이 아닙니다."),
  INVALID_PROFILE_EXCHANGE_STATUS(ErrorType.FAILED_PRECONDITION, "대기 중인 프로필 교환 요청에만 응답할 수 있습니다."),
  SELF_INTRODUCTION_NOT_FOUND(ErrorType.NOT_FOUND, "셀프 소개 글을 찾을 수 없습니다."),
  SELF_INTRODUCTION_ACCESS_DENIED(ErrorType.PERMISSION_DENIED, "셀프 소개 글의 작성자가 아닙니다."),
  INVALID_SELF_INTRODUCTION_TITLE(ErrorType.INVALID_ARGUMENT, "셀프 소개 글의 제목을 입력해 주세요."),
  INVALID_SELF_INTRODUCTION_CONTENT(ErrorType.INVALID_ARGUMENT, "셀프 소개 글은 30자 이상이어야 합니다."),
  INVALID_SELF_INTRODUCTION_IMAGE_URL(ErrorType.INVALID_ARGUMENT, "유효하지 않은 이미지 URL입니다.");

  private final ErrorType type;
  private final String message;
}
