package kr.co.deepple.member.command.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    MEMBER_NOT_FOUND(ErrorType.NOT_FOUND, "회원을 찾을 수 없습니다."),
    MEMBER_DELETED(ErrorType.PERMISSION_DENIED, "탈퇴한 회원입니다."),
    MEMBER_PERMANENTLY_SUSPENDED(ErrorType.PERMISSION_DENIED, "영구 정지된 회원입니다."),
    MEMBER_TEMPORARILY_SUSPENDED(ErrorType.PERMISSION_DENIED, "일시 정지된 회원입니다."),
    MEMBER_NOT_ACTIVE(ErrorType.FAILED_PRECONDITION, "활동 중인 회원이 아닙니다."),
    INVALID_ACTIVITY_STATUS_CHANGE(ErrorType.FAILED_PRECONDITION, "현재 회원 상태에서는 변경할 수 없습니다."),
    AUTH_CODE_NOT_FOUND(ErrorType.NOT_FOUND, "인증번호가 만료되었거나 발송되지 않았습니다."),
    AUTH_CODE_MISMATCH(ErrorType.INVALID_ARGUMENT, "인증번호가 일치하지 않습니다."),
    SMS_SEND_FAILED(ErrorType.UNAVAILABLE, "인증번호 발송에 실패했습니다."),
    PHONE_NUMBER_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 사용 중인 전화번호입니다."),
    KAKAO_ID_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 사용 중인 카카오 아이디입니다."),
    CONTACT_TYPE_SETTING_NEEDED(ErrorType.FAILED_PRECONDITION, "연락 수단 설정이 필요합니다."),
    INVALID_PHONE_NUMBER(ErrorType.INVALID_ARGUMENT, "유효하지 않은 전화번호 형식입니다."),
    INVALID_KAKAO_ID(ErrorType.INVALID_ARGUMENT, "유효하지 않은 카카오 아이디입니다."),
    INVALID_NICKNAME(ErrorType.INVALID_ARGUMENT, "유효하지 않은 닉네임입니다."),
    INVALID_YEAR_OF_BIRTH(ErrorType.INVALID_ARGUMENT, "20세 이상 46세 이하만 가입할 수 있습니다."),
    INVALID_PROFILE_OPTION(ErrorType.INVALID_ARGUMENT, "유효하지 않은 프로필 선택 값입니다."),
    INVALID_GRADE(ErrorType.INVALID_ARGUMENT, "유효하지 않은 회원 등급입니다."),
    PROFILE_ACCESS_DENIED(ErrorType.PERMISSION_DENIED, "프로필에 접근할 권한이 없습니다."),
    PROFILE_IMAGE_COUNT_EXCEEDED(ErrorType.INVALID_ARGUMENT, "프로필 이미지는 6개까지 등록할 수 있습니다."),
    INVALID_PROFILE_IMAGE_EXTENSION(ErrorType.INVALID_ARGUMENT, "지원하지 않는 이미지 확장자입니다."),
    INVALID_PROFILE_IMAGE_ORDER(ErrorType.INVALID_ARGUMENT, "유효하지 않은 프로필 이미지 순서입니다."),
    INVALID_PROFILE_IMAGE_URL(ErrorType.INVALID_ARGUMENT, "유효하지 않은 이미지 URL입니다."),
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
