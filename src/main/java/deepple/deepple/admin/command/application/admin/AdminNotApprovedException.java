package deepple.deepple.admin.command.application.admin;

public class AdminNotApprovedException extends RuntimeException {
    public AdminNotApprovedException() {
        super("승인 대기 중인 관리자입니다.");
    }
}
