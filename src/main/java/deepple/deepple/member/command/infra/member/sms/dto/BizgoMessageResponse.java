package deepple.deepple.member.command.infra.member.sms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BizgoMessageResponse(
    CommonField common,
    Data data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CommonField(
        String authCode,
        String authResult,
        String infobankTrId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
        String code,
        String result,
        Object data
    ) {}
}
