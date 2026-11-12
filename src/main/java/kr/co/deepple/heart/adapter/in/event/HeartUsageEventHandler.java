package kr.co.deepple.heart.adapter.in.event;

import kr.co.deepple.community.command.domain.profileexchange.event.ProfileExchangeRequestedEvent;
import kr.co.deepple.heart.application.HeartUsagePolicyService;
import kr.co.deepple.heart.domain.TransactionSubtype;
import kr.co.deepple.heart.domain.TransactionType;
import kr.co.deepple.match.command.domain.match.event.MatchAcceptedEvent;
import kr.co.deepple.match.command.domain.match.event.MatchRequestedEvent;
import kr.co.deepple.member.command.domain.introduction.event.MemberIntroducedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HeartUsageEventHandler {
    private final HeartUsagePolicyService heartUsageService;

    @EventListener(value = MatchRequestedEvent.class)
    public void handle(MatchRequestedEvent event) {
        heartUsageService.useHeart(event.getRequesterId(), TransactionType.MESSAGE,
            TransactionType.MESSAGE.getDescription(), event.getMatchType());
    }

    @EventListener(value = MemberIntroducedEvent.class)
    public void handle(MemberIntroducedEvent event) {
        heartUsageService.useHeart(event.getMemberId(), TransactionType.INTRODUCTION, event.getContent(),
            event.getIntroductionType());
    }

    @EventListener(value = MatchAcceptedEvent.class)
    public void handle(MatchAcceptedEvent event) {
        heartUsageService.useHeart(event.getRequesterId(), TransactionType.MESSAGE_ACCEPTED,
            TransactionType.MESSAGE_ACCEPTED.getDescription(), TransactionSubtype.MATCH_ACCEPTED.name());
    }

    @EventListener(value = ProfileExchangeRequestedEvent.class)
    public void handle(ProfileExchangeRequestedEvent event) {
        heartUsageService.useHeart(event.getRequesterId(), TransactionType.PROFILE_EXCHANGE,
            TransactionType.PROFILE_EXCHANGE.getDescription(), TransactionSubtype.PROFILE_EXCHANGE.name());
    }
}
