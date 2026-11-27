package kr.co.deepple.conversation.application;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kr.co.deepple.block.application.port.in.FindBlockedMemberIdsUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.application.port.in.FindProfileExchangeHistoriesUseCase;
import kr.co.deepple.community.application.port.in.ProfileExchangeHistory;
import kr.co.deepple.conversation.application.dto.ConversationContactView;
import kr.co.deepple.conversation.application.dto.ConversationDetailView;
import kr.co.deepple.conversation.application.dto.ConversationView;
import kr.co.deepple.conversation.application.dto.ConversationViews;
import kr.co.deepple.conversation.domain.ConversationErrorCode;
import kr.co.deepple.match.application.port.in.FindMatchHistoriesUseCase;
import kr.co.deepple.match.application.port.in.MatchHistory;
import kr.co.deepple.member.application.port.in.FindMemberPreviewsUseCase;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.application.port.in.GetMemberDetailUseCase;
import kr.co.deepple.member.application.port.in.MemberDetail;
import kr.co.deepple.member.application.port.in.MemberPreview;
import kr.co.deepple.member.application.port.in.MemberSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationQueryService {

  private static final int PAGE_SIZE = 20;
  private static final String PHONE_NUMBER = "PHONE_NUMBER";
  private static final String KAKAO = "KAKAO";

  private final FindMatchHistoriesUseCase findMatchHistoriesUseCase;
  private final FindProfileExchangeHistoriesUseCase findProfileExchangeHistoriesUseCase;
  private final FindBlockedMemberIdsUseCase findBlockedMemberIdsUseCase;
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final FindMemberPreviewsUseCase findMemberPreviewsUseCase;
  private final GetMemberDetailUseCase getMemberDetailUseCase;

  public ConversationViews findConversations(long memberId, String cursor) {
    Optional<ConversationCursor> after = ConversationCursor.parse(cursor);
    List<SortedConversation> sorted =
        findConversationsOf(memberId).stream()
            .map(conversation -> new SortedConversation(conversation, cursorOf(conversation)))
            .filter(sortedConversation -> isAfter(sortedConversation.cursor(), after))
            .sorted(Comparator.comparing(SortedConversation::cursor))
            .toList();
    boolean hasMore = sorted.size() > PAGE_SIZE;
    List<SortedConversation> page = hasMore ? sorted.subList(0, PAGE_SIZE) : sorted;
    Map<Long, MemberPreview> previews =
        findMemberPreviewsUseCase.findMemberPreviews(
            page.stream()
                .map(sortedConversation -> sortedConversation.conversation().counterpartId())
                .toList());
    List<ConversationView> views =
        page.stream()
            .map(SortedConversation::conversation)
            .map(conversation -> toView(conversation, previews.get(conversation.counterpartId())))
            .toList();
    String nextCursor = hasMore ? page.getLast().cursor().toCursor() : null;
    return new ConversationViews(views, nextCursor, hasMore);
  }

  public ConversationDetailView getConversation(long memberId, long counterpartId) {
    if (findBlockedMemberIdsUseCase.findBlockRelatedMemberIds(memberId).contains(counterpartId)) {
      throw notFound(memberId, counterpartId);
    }
    MemberSummary counterpart =
        findMemberSummaryUseCase.findMemberSummaries(List.of(counterpartId)).get(counterpartId);
    List<MatchHistory> matches =
        findMatchHistoriesUseCase.findMatchHistoriesBetween(memberId, counterpartId);
    ProfileExchangeHistory exchange =
        findProfileExchangeHistoriesUseCase
            .findProfileExchangeHistoryBetween(memberId, counterpartId)
            .orElse(null);
    if (counterpart == null || (matches.isEmpty() && exchange == null)) {
      throw notFound(memberId, counterpartId);
    }
    Conversation conversation =
        new Conversation(memberId, counterpartId, matches, exchange, counterpart.isActive());
    MemberPreview preview =
        findMemberPreviewsUseCase.findMemberPreviews(List.of(counterpartId)).get(counterpartId);
    return new ConversationDetailView(
        counterpartId,
        preview == null ? null : preview.nickname(),
        preview == null ? null : preview.profileImageUrl(),
        preview == null ? null : preview.age(),
        preview == null ? null : preview.gender(),
        conversation.events(),
        conversation.matchView(),
        conversation.profileExchangeView(),
        findContact(conversation),
        conversation.needsMyAction(),
        conversation.counterpartActive());
  }

  private List<Conversation> findConversationsOf(long memberId) {
    Set<Long> blocked = findBlockedMemberIdsUseCase.findBlockRelatedMemberIds(memberId);
    Map<Long, List<MatchHistory>> matchesByCounterpart =
        findMatchHistoriesUseCase.findMatchHistoriesOf(memberId).stream()
            .collect(Collectors.groupingBy(match -> counterpartOf(memberId, match)));
    Map<Long, ProfileExchangeHistory> exchangeByCounterpart =
        findProfileExchangeHistoriesUseCase.findProfileExchangeHistoriesOf(memberId).stream()
            .collect(
                Collectors.toMap(
                    exchange -> counterpartOf(memberId, exchange),
                    Function.identity(),
                    (first, second) -> first));
    Set<Long> counterpartIds =
        Stream.concat(
                matchesByCounterpart.keySet().stream(), exchangeByCounterpart.keySet().stream())
            .filter(counterpartId -> !blocked.contains(counterpartId))
            .collect(Collectors.toSet());
    Map<Long, MemberSummary> counterparts =
        findMemberSummaryUseCase.findMemberSummaries(counterpartIds);
    return counterpartIds.stream()
        .filter(counterparts::containsKey)
        .map(counterpartId ->
            new Conversation(
                memberId,
                counterpartId,
                matchesByCounterpart.getOrDefault(counterpartId, List.of()),
                exchangeByCounterpart.get(counterpartId),
                counterparts.get(counterpartId).isActive()))
        .toList();
  }

  private ConversationView toView(Conversation conversation, MemberPreview preview) {
    return new ConversationView(
        conversation.counterpartId(),
        preview == null ? null : preview.nickname(),
        preview == null ? null : preview.profileImageUrl(),
        conversation.lastEvent(),
        conversation.counterpartMessage(),
        conversation.needsMyAction(),
        conversation.counterpartActive(),
        conversation.lastActivityAt());
  }

  private ConversationContactView findContact(Conversation conversation) {
    if (!conversation.counterpartActive()) {
      return null;
    }
    return conversation
        .counterpartContactType()
        .map(contactType -> toContact(conversation.counterpartId(), contactType))
        .orElse(null);
  }

  private ConversationContactView toContact(long counterpartId, String contactType) {
    MemberDetail counterpart = getMemberDetailUseCase.getMemberDetail(counterpartId);
    if (PHONE_NUMBER.equals(contactType)) {
      return new ConversationContactView(contactType, counterpart.phoneNumber());
    }
    if (KAKAO.equals(contactType)) {
      return new ConversationContactView(contactType, counterpart.kakaoId());
    }
    throw new IllegalStateException(
        "매칭의 연락 수단을 알 수 없습니다. counterpartId=" + counterpartId + ", contactType=" + contactType);
  }

  private static boolean isAfter(ConversationCursor cursor, Optional<ConversationCursor> after) {
    return after.map(afterCursor -> cursor.compareTo(afterCursor) > 0).orElse(true);
  }

  private static ConversationCursor cursorOf(Conversation conversation) {
    return ConversationCursor.of(conversation.lastActivityAt(), conversation.counterpartId());
  }

  private static long counterpartOf(long memberId, MatchHistory match) {
    return match.requesterId() == memberId ? match.responderId() : match.requesterId();
  }

  private static long counterpartOf(long memberId, ProfileExchangeHistory exchange) {
    return exchange.requesterId() == memberId ? exchange.responderId() : exchange.requesterId();
  }

  private static DeeppleException notFound(long memberId, long counterpartId) {
    return new DeeppleException(
        ConversationErrorCode.CONVERSATION_NOT_FOUND,
        "memberId=" + memberId + ", counterpartId=" + counterpartId);
  }

  private record SortedConversation(Conversation conversation, ConversationCursor cursor) {}
}
