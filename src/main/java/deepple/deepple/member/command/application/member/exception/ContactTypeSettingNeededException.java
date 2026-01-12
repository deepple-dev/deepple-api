package deepple.deepple.member.command.application.member.exception;

public class ContactTypeSettingNeededException extends RuntimeException {
    public ContactTypeSettingNeededException() {
        super("연락 타입 설정이 필요합니다.");
    }
}
