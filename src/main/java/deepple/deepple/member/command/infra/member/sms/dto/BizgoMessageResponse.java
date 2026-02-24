package deepple.deepple.member.command.infra.member.sms.dto;

public record BizgoMessageResponse(
    CommonField common,
    Data data
) {
    public record CommonField(
        String authCode,
        String authResult,
        String infobankTrId
    ) {}

    public record Data(
        String code,
        String result,
        Object data
    ) {}
}
