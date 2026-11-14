package kr.co.deepple.member.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
  MEMBER_NOT_FOUND(ErrorType.NOT_FOUND, "회원을 찾을 수 없습니다."),
  INVALID_ACTIVITY_STATUS_CHANGE(ErrorType.FAILED_PRECONDITION, "현재 회원 상태에서는 변경할 수 없습니다."),
  PHONE_NUMBER_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 사용 중인 전화번호입니다."),
  KAKAO_ID_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 사용 중인 카카오 아이디입니다."),
  CONTACT_TYPE_SETTING_NEEDED(ErrorType.FAILED_PRECONDITION, "연락 수단 설정이 필요합니다."),
  INVALID_PHONE_NUMBER(ErrorType.INVALID_ARGUMENT, "유효하지 않은 전화번호 형식입니다."),
  INVALID_KAKAO_ID(ErrorType.INVALID_ARGUMENT, "유효하지 않은 카카오 아이디입니다."),
  INVALID_NICKNAME(ErrorType.INVALID_ARGUMENT, "유효하지 않은 닉네임입니다."),
  INVALID_YEAR_OF_BIRTH(ErrorType.INVALID_ARGUMENT, "20세 이상 46세 이하만 가입할 수 있습니다."),
  INVALID_PROFILE_OPTION(ErrorType.INVALID_ARGUMENT, "유효하지 않은 프로필 선택 값입니다."),
  INVALID_GRADE(ErrorType.INVALID_ARGUMENT, "유효하지 않은 회원 등급입니다."),
  PROFILE_IMAGE_COUNT_EXCEEDED(ErrorType.INVALID_ARGUMENT, "프로필 이미지는 6개까지 등록할 수 있습니다."),
  INVALID_PROFILE_IMAGE_EXTENSION(ErrorType.INVALID_ARGUMENT, "지원하지 않는 이미지 확장자입니다."),
  INVALID_PROFILE_IMAGE_ORDER(ErrorType.INVALID_ARGUMENT, "유효하지 않은 프로필 이미지 순서입니다."),
  INVALID_PROFILE_IMAGE_URL(ErrorType.INVALID_ARGUMENT, "유효하지 않은 이미지 URL입니다.");

  private final ErrorType type;
  private final String message;
}
