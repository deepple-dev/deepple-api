package deepple.deepple.member.command.infra.member.sms.dto;

import java.util.List;

public record BizgoMessageRequest(
    List<MessageFlow> messageFlow,
    List<Destination> destinations,
    String ref
) {

    public record MessageFlow(Sms sms) {}
    public record Sms(String from, String text) {}
    public record Destination(String to) {}

    public BizgoMessageRequest(String text, String from, String to) {
        this(
            List.of(new MessageFlow(new Sms(from, text))),
            List.of(new Destination(to)),
            null
        );
    }
}