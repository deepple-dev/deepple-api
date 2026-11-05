package deepple.deepple.admin.presentation.admin;

import deepple.deepple.admin.command.application.admin.AdminNotApprovedException;
import deepple.deepple.admin.command.application.admin.AdminNotFoundException;
import deepple.deepple.admin.command.application.admin.DuplicateEmailException;
import deepple.deepple.admin.command.application.admin.PendingAdminNotFoundException;
import deepple.deepple.admin.command.domain.admin.exception.IncorrectPasswordException;
import deepple.deepple.admin.command.domain.admin.exception.InvalidAdminRoleException;
import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<BaseResponse<Void>> handleDuplicateEmailException(DuplicateEmailException e) {
        log.warn("관리자 회원가입에 실패했습니다. {}", e.getMessage());

        return ResponseEntity.badRequest()
            .body(BaseResponse.of(StatusType.INVALID_DUPLICATE_VALUE, e.getMessage()));
    }

    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleAdminNotFoundException(AdminNotFoundException e) {
        log.warn("관리자 로그인에 실패했습니다. {}", e.getMessage());

        return ResponseEntity.status(401)
            .body(BaseResponse.of(StatusType.UNAUTHORIZED, "자격 증명이 유효하지 않습니다."));
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<BaseResponse<Void>> handlePasswordMismatchException(IncorrectPasswordException e) {
        log.warn("관리자 로그인에 실패했습니다. {}", e.getMessage());

        return ResponseEntity.status(401)
            .body(BaseResponse.of(StatusType.UNAUTHORIZED, "자격 증명이 유효하지 않습니다."));
    }

    @ExceptionHandler(AdminNotApprovedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAdminNotApprovedException(AdminNotApprovedException e) {
        log.warn("관리자 로그인에 실패했습니다. {}", e.getMessage());

        return ResponseEntity.status(403)
            .body(BaseResponse.of(StatusType.ADMIN_NOT_APPROVED, e.getMessage()));
    }

    @ExceptionHandler(PendingAdminNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handlePendingAdminNotFoundException(PendingAdminNotFoundException e) {
        log.warn(e.getMessage());

        return ResponseEntity.status(404)
            .body(BaseResponse.of(StatusType.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(InvalidAdminRoleException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidAdminRoleException(InvalidAdminRoleException e) {
        log.warn(e.getMessage());

        return ResponseEntity.badRequest()
            .body(BaseResponse.of(StatusType.INVALID_TYPE_VALUE, e.getMessage()));
    }
}
