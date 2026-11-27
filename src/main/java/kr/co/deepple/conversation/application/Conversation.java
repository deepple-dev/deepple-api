package kr.co.deepple.conversation.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.community.application.port.in.ProfileExchangeHistory;
import kr.co.deepple.conversation.application.dto.ConversationEventView;
import kr.co.deepple.conversation.application.dto.ConversationMatchView;
import kr.co.deepple.conversation.application.dto.ConversationProfileExchangeView;
import kr.co.deepple.conversation.domain.ConversationEventType;
import kr.co.deepple.match.application.port.in.MatchHistory;

record Conversation(
    long memberId,
    long counterpartId,
    List<MatchHistory> matches,
    ProfileExchangeHistory profileExchange,
    boolean counterpartActive) {

  private static final String WAITING = "WAITING";
  private static final String MATCHED = "MATCHED";
  private static final String REJECTED = "REJECTED";
  private static final String REJECT_CHECKED = "REJECT_CHECKED";
  private static final String EXPIRED = "EXPIRED";
  private static final String APPROVE = "APPROVE";
  private static final String SENT = "SENT";
  private static final String RECEIVED = "RECEIVED";

  List<ConversationEventView> events() {
    List<ConversationEventView> events = new ArrayList<>();
    if (profileExchange != null) {
      events.addAll(exchangeEvents(profileExchange));
    }
    matches.stream()
        .sorted(Comparator.comparing(MatchHistory::requestedAt))
        .forEach(match -> events.addAll(matchEvents(match)));
    return events.stream().sorted(Comparator.comparing(ConversationEventView::occurredAt)).toList();
  }

  ConversationEventView lastEvent() {
    return events().getLast();
  }

  LocalDateTime lastActivityAt() {
    return lastEvent().occurredAt();
  }

  boolean needsMyAction() {
    if (!counterpartActive) {
      return false;
    }
    boolean receivedMessage =
        activeMatch()
            .filter(match -> WAITING.equals(match.status()) && match.responderId() == memberId)
            .isPresent();
    boolean receivedExchange =
        profileExchange != null
            && WAITING.equals(profileExchange.status())
            && profileExchange.responderId() == memberId;
    return receivedMessage || receivedExchange;
  }

  String counterpartMessage() {
    return latestMatch()
        .map(match ->
            match.responderId() == memberId ? match.requestMessage() : match.responseMessage())
        .orElse(null);
  }

  Optional<MatchHistory> activeMatch() {
    return matches.stream()
        .filter(match -> !EXPIRED.equals(match.status()) && !REJECT_CHECKED.equals(match.status()))
        .findFirst();
  }

  ConversationMatchView matchView() {
    return activeMatch()
        .map(match -> {
          boolean isRequester = match.requesterId() == memberId;
          return new ConversationMatchView(
              match.matchId(),
              match.status(),
              isRequester,
              isRequester ? match.readByResponderAt() : null);
        })
        .orElse(null);
  }

  ConversationProfileExchangeView profileExchangeView() {
    if (profileExchange == null) {
      return null;
    }
    return new ConversationProfileExchangeView(
        profileExchange.profileExchangeId(),
        profileExchange.status(),
        profileExchange.requesterId() == memberId ? SENT : RECEIVED);
  }

  Optional<String> counterpartContactType() {
    return activeMatch()
        .filter(match -> MATCHED.equals(match.status()))
        .map(match ->
            match.requesterId() == counterpartId
                ? match.requesterContactType()
                : match.responderContactType());
  }

  private Optional<MatchHistory> latestMatch() {
    return activeMatch()
        .or(() -> matches.stream().max(Comparator.comparing(MatchHistory::requestedAt)));
  }

  private List<ConversationEventView> exchangeEvents(ProfileExchangeHistory exchange) {
    List<ConversationEventView> events = new ArrayList<>();
    events.add(
        exchangeEvent(
            ConversationEventType.PROFILE_EXCHANGE_REQUESTED,
            exchange.requesterId(),
            exchange.requestedAt(),
            exchange));
    if (APPROVE.equals(exchange.status())) {
      events.add(
          exchangeEvent(
              ConversationEventType.PROFILE_EXCHANGE_APPROVED,
              exchange.responderId(),
              respondedAtOf(exchange),
              exchange));
    }
    if (REJECTED.equals(exchange.status())) {
      events.add(
          exchangeEvent(
              ConversationEventType.PROFILE_EXCHANGE_REJECTED,
              exchange.responderId(),
              respondedAtOf(exchange),
              exchange));
    }
    return events;
  }

  private List<ConversationEventView> matchEvents(MatchHistory match) {
    List<ConversationEventView> events = new ArrayList<>();
    events.add(
        matchEvent(
            ConversationEventType.MESSAGE_SENT,
            match.requesterId() == memberId,
            match.requestedAt(),
            match.requestMessage(),
            match));
    boolean respondedByMe = match.responderId() == memberId;
    switch (match.status()) {
      case MATCHED ->
          events.add(
              matchEvent(
                  ConversationEventType.MESSAGE_ACCEPTED,
                  respondedByMe,
                  respondedAtOf(match),
                  match.responseMessage(),
                  match));
      case REJECTED, REJECT_CHECKED ->
          events.add(
              matchEvent(
                  ConversationEventType.MESSAGE_REJECTED,
                  respondedByMe,
                  respondedAtOf(match),
                  null,
                  match));
      case EXPIRED ->
          events.add(
              matchEvent(
                  ConversationEventType.MESSAGE_EXPIRED,
                  false,
                  match.expiredAt() == null ? match.requestedAt() : match.expiredAt(),
                  null,
                  match));
      default -> {}
    }
    return events;
  }

  private ConversationEventView exchangeEvent(
      ConversationEventType type,
      long actorId,
      LocalDateTime occurredAt,
      ProfileExchangeHistory exchange) {
    return new ConversationEventView(
        type.name(), actorId == memberId, occurredAt, null, null, exchange.profileExchangeId());
  }

  private ConversationEventView matchEvent(
      ConversationEventType type,
      boolean isMine,
      LocalDateTime occurredAt,
      String message,
      MatchHistory match) {
    return new ConversationEventView(
        type.name(), isMine, occurredAt, message, match.matchId(), null);
  }

  private static LocalDateTime respondedAtOf(MatchHistory match) {
    return match.respondedAt() == null ? match.requestedAt() : match.respondedAt();
  }

  private static LocalDateTime respondedAtOf(ProfileExchangeHistory exchange) {
    return exchange.respondedAt() == null ? exchange.requestedAt() : exchange.respondedAt();
  }
}
