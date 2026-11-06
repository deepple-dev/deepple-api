package deepple.deepple.match.command.domain.match;

import deepple.deepple.common.exception.ErrorCode;
import deepple.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatchErrorCode implements ErrorCode {
    MATCH_NOT_FOUND(ErrorType.NOT_FOUND, "매치를 찾을 수 없습니다."),
    MATCH_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 상대방과의 매치가 존재합니다."),
    INVALID_MATCH_STATUS_CHANGE(ErrorType.FAILED_PRECONDITION, "현재 상태에서는 변경할 수 없는 매치입니다."),
    INVALID_MATCH_MESSAGE(ErrorType.INVALID_ARGUMENT, "메시지를 입력해 주세요."),
    INVALID_MATCH_CONTACT_TYPE(ErrorType.INVALID_ARGUMENT, "유효하지 않은 연락 수단입니다."),
    MATCH_ACCESS_DENIED(ErrorType.PERMISSION_DENIED, "매치 당사자가 아닙니다."),
    MATCH_REQUESTER_INACTIVE(ErrorType.FAILED_PRECONDITION, "활동 중이 아닌 회원의 매치 요청입니다.");

    private final ErrorType type;
    private final String message;
}
