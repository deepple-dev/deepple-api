package deepple.deepple.member.command.infra.member;

import deepple.deepple.match.command.domain.match.event.MatchRequestedEvent;
import deepple.deepple.match.command.domain.match.event.MatchRespondedEvent;
import deepple.deepple.member.command.application.member.MemberProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberProfileEventHandler {

    private final MemberProfileService memberProfileService;

    @EventListener(value = MatchRequestedEvent.class)
    public void handle(MatchRequestedEvent event) {
        memberProfileService.validateContactTypeSetting(event.getRequesterId(), event.getContactType());
    }

    @EventListener(value = MatchRespondedEvent.class)
    public void handle(MatchRespondedEvent event) {
        memberProfileService.validateContactTypeSetting(event.getResponderId(), event.getContactType());
    }
}
