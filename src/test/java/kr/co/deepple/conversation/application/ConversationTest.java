package kr.co.deepple.conversation.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import kr.co.deepple.community.application.port.in.ProfileExchangeHistory;
import kr.co.deepple.conversation.application.dto.ConversationEventView;
import kr.co.deepple.match.application.port.in.MatchHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConversationTest {

  private static final long ME = 1L;
  private static final long OTHER = 2L;
  private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 20, 10, 0);

  @Test
  @DisplayName("교환 요청·수락과 메시지·답장을 시간 순 타임라인으로 만든다.")
  void buildTimelineInOrder() {
    ProfileExchangeHistory exchange =
        new ProfileExchangeHistory(5L, OTHER, ME, "APPROVE", T0, T0.plusHours(1));
    MatchHistory match =
        match(10L, ME, OTHER, "MATCHED", T0.plusHours(2), T0.plusHours(3), "안녕하세요", "반가워요");

    List<ConversationEventView> events =
        new Conversation(ME, OTHER, List.of(match), exchange, true).events();

    assertThat(events)
        .extracting(ConversationEventView::type)
        .containsExactly(
            "PROFILE_EXCHANGE_REQUESTED",
            "PROFILE_EXCHANGE_APPROVED",
            "MESSAGE_SENT",
            "MESSAGE_ACCEPTED");
    assertThat(events)
        .extracting(ConversationEventView::isMine)
        .containsExactly(false, true, true, false);
    assertThat(events.get(3).message()).isEqualTo("반가워요");
    assertThat(events.get(3).matchId()).isEqualTo(10L);
    assertThat(events.get(0).profileExchangeId()).isEqualTo(5L);
  }

  @Test
  @DisplayName("받은 메시지가 대기 중이면 내가 답해야 하고, 상대 메시지를 미리보기로 준다.")
  void needsMyActionWhenReceivedWaitingMessage() {
    MatchHistory received = match(10L, OTHER, ME, "WAITING", T0, null, "안녕하세요", null);

    Conversation conversation = new Conversation(ME, OTHER, List.of(received), null, true);

    assertThat(conversation.needsMyAction()).isTrue();
    assertThat(conversation.counterpartMessage()).isEqualTo("안녕하세요");
    assertThat(conversation.lastEvent().type()).isEqualTo("MESSAGE_SENT");
    assertThat(conversation.lastEvent().isMine()).isFalse();
    assertThat(conversation.matchView().isRequester()).isFalse();
    assertThat(conversation.matchView().readAt()).isNull();
  }

  @Test
  @DisplayName("내가 보낸 메시지가 대기 중이면 답할 일이 없고, 상대가 읽은 시각을 준다.")
  void noActionWhenSentWaitingMessage() {
    MatchHistory sent =
        new MatchHistory(
            10L,
            ME,
            OTHER,
            "안녕하세요",
            null,
            "WAITING",
            "PHONE_NUMBER",
            null,
            T0,
            null,
            null,
            T0.plusMinutes(5));

    Conversation conversation = new Conversation(ME, OTHER, List.of(sent), null, true);

    assertThat(conversation.needsMyAction()).isFalse();
    assertThat(conversation.counterpartMessage()).isNull();
    assertThat(conversation.matchView().readAt()).isEqualTo(T0.plusMinutes(5));
  }

  @Test
  @DisplayName("거절 확인된 지난 매칭은 타임라인에 남고, 진행 중인 매칭은 없다.")
  void keepPastRejectedMatchInTimeline() {
    MatchHistory rejected =
        match(10L, ME, OTHER, "REJECT_CHECKED", T0, T0.plusHours(1), "안녕하세요", null);

    Conversation conversation = new Conversation(ME, OTHER, List.of(rejected), null, true);

    assertThat(conversation.events())
        .extracting(ConversationEventView::type)
        .containsExactly("MESSAGE_SENT", "MESSAGE_REJECTED");
    assertThat(conversation.matchView()).isNull();
    assertThat(conversation.lastActivityAt()).isEqualTo(T0.plusHours(1));
  }

  @Test
  @DisplayName("받은 교환 요청이 대기 중이면 내가 답해야 하고, 신청 방향은 RECEIVED다.")
  void needsMyActionWhenReceivedWaitingExchange() {
    ProfileExchangeHistory exchange =
        new ProfileExchangeHistory(5L, OTHER, ME, "WAITING", T0, null);

    Conversation conversation = new Conversation(ME, OTHER, List.of(), exchange, true);

    assertThat(conversation.needsMyAction()).isTrue();
    assertThat(conversation.profileExchangeView().direction()).isEqualTo("RECEIVED");
    assertThat(conversation.counterpartContactType()).isEmpty();
  }

  @Test
  @DisplayName("매칭이 성사되면 상대가 공개한 연락 수단을 고른다.")
  void counterpartContactTypeWhenMatched() {
    MatchHistory matched =
        new MatchHistory(
            10L,
            OTHER,
            ME,
            "안녕하세요",
            "반가워요",
            "MATCHED",
            "KAKAO",
            "PHONE_NUMBER",
            T0,
            T0.plusHours(1),
            null,
            null);

    Conversation conversation = new Conversation(ME, OTHER, List.of(matched), null, true);

    assertThat(conversation.counterpartContactType()).contains("KAKAO");
  }

  @Test
  @DisplayName("상대가 활동 중이 아니면 받은 메시지가 대기 중이어도 답할 일로 보지 않는다.")
  void noActionWhenCounterpartInactive() {
    MatchHistory received = match(10L, OTHER, ME, "WAITING", T0, null, "안녕하세요", null);

    Conversation conversation = new Conversation(ME, OTHER, List.of(received), null, false);

    assertThat(conversation.needsMyAction()).isFalse();
  }

  @Test
  @DisplayName("지난 거절 매칭 뒤 새 매칭이 진행 중이면, 진행 중인 매칭 기준으로 상태와 미리보기를 정하고 타임라인은 모두 남긴다.")
  void useActiveMatchAfterPastRejectedMatch() {
    MatchHistory past =
        match(10L, OTHER, ME, "REJECT_CHECKED", T0, T0.plusHours(1), "예전 메시지", null);
    MatchHistory active = match(11L, OTHER, ME, "WAITING", T0.plusDays(1), null, "다시 연락드려요", null);

    Conversation conversation = new Conversation(ME, OTHER, List.of(active, past), null, true);

    assertThat(conversation.matchView().matchId()).isEqualTo(11L);
    assertThat(conversation.counterpartMessage()).isEqualTo("다시 연락드려요");
    assertThat(conversation.needsMyAction()).isTrue();
    assertThat(conversation.events())
        .extracting(ConversationEventView::type)
        .containsExactly("MESSAGE_SENT", "MESSAGE_REJECTED", "MESSAGE_SENT");
  }

  private static MatchHistory match(
      long id,
      long requesterId,
      long responderId,
      String status,
      LocalDateTime requestedAt,
      LocalDateTime respondedAt,
      String requestMessage,
      String responseMessage) {
    return new MatchHistory(
        id,
        requesterId,
        responderId,
        requestMessage,
        responseMessage,
        status,
        "PHONE_NUMBER",
        responseMessage == null ? null : "KAKAO",
        requestedAt,
        respondedAt,
        null,
        null);
  }
}
