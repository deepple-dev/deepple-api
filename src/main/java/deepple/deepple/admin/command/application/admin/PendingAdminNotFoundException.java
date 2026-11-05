package deepple.deepple.admin.command.application.admin;

public class PendingAdminNotFoundException extends RuntimeException {
    public PendingAdminNotFoundException() {
        super("승인 대기 중인 관리자를 찾을 수 없습니다.");
    }
}
