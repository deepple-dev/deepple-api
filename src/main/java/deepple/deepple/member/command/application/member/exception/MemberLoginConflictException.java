package deepple.deepple.member.command.application.member.exception;

public class MemberLoginConflictException extends RuntimeException {
    public MemberLoginConflictException() {
        super("로그인 충돌이 감지되었습니다. 잠시 후 다시 시도해주세요.");
    }
}
