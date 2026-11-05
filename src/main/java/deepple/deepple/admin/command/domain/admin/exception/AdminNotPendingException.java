package deepple.deepple.admin.command.domain.admin.exception;

public class AdminNotPendingException extends RuntimeException {
    public AdminNotPendingException() {
        super("승인 대기 중인 관리자만 승인할 수 있습니다.");
    }
}
