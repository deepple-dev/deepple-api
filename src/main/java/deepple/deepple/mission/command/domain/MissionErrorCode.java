package deepple.deepple.mission.command.domain;

import deepple.deepple.common.exception.ErrorCode;
import deepple.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MissionErrorCode implements ErrorCode {
    GENDER_NOT_SET(ErrorType.FAILED_PRECONDITION, "성별 설정이 필요합니다.");

    private final ErrorType type;
    private final String message;
}
