package deepple.deepple.admin.command.domain.admin.exception;

public class InvalidAdminRoleException extends RuntimeException {
    public InvalidAdminRoleException(String value) {
        super("유효하지 않은 관리자 역할입니다: " + value);
    }
}
