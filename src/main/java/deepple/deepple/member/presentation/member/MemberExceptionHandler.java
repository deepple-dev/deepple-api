package deepple.deepple.member.presentation.member;

import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.member.command.application.member.exception.*;
import deepple.deepple.member.command.domain.member.exception.MemberNotActiveException;
import deepple.deepple.member.presentation.member.dto.TemporarySuspensionLoginResponse;
import deepple.deepple.member.query.member.application.exception.ProfileAccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MemberExceptionHandler {

    @ExceptionHandler(PermanentlySuspendedMemberException.class)
    public ResponseEntity<BaseResponse<Void>> handlePermanentlySuspendedMemberException(
        PermanentlySuspendedMemberException e
    ) {
        log.warn("멤버 로그인에 실패하였습니다. {}", e.getMessage());

        return ResponseEntity.status(403)
            .body(BaseResponse.of(StatusType.FORBIDDEN, e.getMessage()));
    }

    @ExceptionHandler(TemporarilySuspendedMemberException.class)
    public ResponseEntity<BaseResponse<TemporarySuspensionLoginResponse>> handleTemporarilySuspendedMemberException(
        TemporarilySuspendedMemberException e
    ) {
        if (e.getSuspensionExpireAt() == null) {
            log.error("일시 정지 만료일이 존재하지 않습니다.", e);
        }
        log.warn("멤버 로그인에 실패하였습니다. {}", e.getMessage());

        TemporarySuspensionLoginResponse response = new TemporarySuspensionLoginResponse(
            e.getMessage(), e.getSuspensionExpireAt());

        return ResponseEntity.status(403)
            .body(BaseResponse.of(StatusType.TEMPORARILY_FORBIDDEN, response));
    }

    @ExceptionHandler(MemberDeletedException.class)
    public ResponseEntity<BaseResponse<Void>> handleMemberDeletedException(MemberDeletedException e) {
        log.warn("멤버 로그인에 실패하였습니다. {}", e.getMessage());

        return ResponseEntity.status(403)
            .body(BaseResponse.of(StatusType.DELETED_TARGET, e.getMessage()));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleMemberNotFoundException(MemberNotFoundException e) {
        log.warn("멤버 조회에 실패하였습니다. {}", e.getMessage());

        return ResponseEntity.status(404)
            .body(BaseResponse.of(StatusType.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<BaseResponse<Void>> handlePhoneNumberAlreadyExistsException(
        PhoneNumberAlreadyExistsException e) {
        log.warn("휴대폰 번호 변경에 실패하였습니다. {}", e.getMessage());

        return ResponseEntity.badRequest()
            .body(BaseResponse.of(StatusType.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(KakaoIdAlreadyExistsException.class)
    public ResponseEntity<BaseResponse<Void>> handleKakaoIdAlreadyExistsException(KakaoIdAlreadyExistsException e) {
        log.warn("카카오 아이디 변경에 실패하였습니다. {}", e.getMessage());

        return ResponseEntity.badRequest()
            .body(BaseResponse.of(StatusType.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(ContactTypeSettingNeededException.class)
    public ResponseEntity<BaseResponse<Void>> handlePrimaryContactTypeSettingNeededException(
        ContactTypeSettingNeededException e) {
        log.warn("매치 요청/응답에 실패하였습니다. {}", e.getMessage());

        return ResponseEntity.badRequest()
            .body(BaseResponse.of(StatusType.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(ProfileAccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleProfileAccessDeniedException(
        ProfileAccessDeniedException e) {
        log.warn("프로필 조회에 실패하였습니다. {}", e.getMessage());

        return ResponseEntity.status(403)
            .body(BaseResponse.of(StatusType.FORBIDDEN, e.getMessage()));
    }

    @ExceptionHandler(CodeNotExistsException.class)
    public ResponseEntity<BaseResponse<Void>> handleCodeNotExistsException(CodeNotExistsException e) {
        log.warn("코드 인증에 실패하였습니다. {}", e.getMessage());

        return ResponseEntity.status(404)
            .body(BaseResponse.of(StatusType.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(CodeNotMatchException.class)
    public ResponseEntity<BaseResponse<Void>> handleCodeNotMatchException(CodeNotMatchException e) {
        log.warn("코드가 일치하지 않습니다. {}", e.getMessage());

        return ResponseEntity.badRequest()
            .body(BaseResponse.of(StatusType.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(MemberNotActiveException.class)
    public ResponseEntity<BaseResponse<Void>> handleMemberNotActiveException(MemberNotActiveException e) {
        log.warn("회원의 상태가 부적절합니다. {}", e.getMessage());

        return ResponseEntity.badRequest()
            .body(BaseResponse.of(StatusType.DORMANT_STATUS, e.getMessage()));
    }
}
