package deepple.deepple.member.command.infra.member.sms.dto;

import lombok.AllArgsConstructor;

import java.util.List;

public class BizgoMessageRequest {
    List<MessageFlow> messageFlow;
    List<Destination> destinations;
    String ref;

    @AllArgsConstructor
    public class MessageFlow {
        Sms sms;
    }

    @AllArgsConstructor
    public class Sms {
        String from;
        String text;
    }

    @AllArgsConstructor
    public class Destination {
        String to;
    }

    public BizgoMessageRequest(String text, String from, String to) {
        Destination destination = new Destination(to);
        Sms sms = new Sms(from, text);
        MessageFlow messageFlow = new MessageFlow(sms);

        this.messageFlow = List.of(messageFlow);
        this.destinations = List.of(destination);
        this.ref = null;
    }
}