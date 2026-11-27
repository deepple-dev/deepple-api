package kr.co.deepple.conversation.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.conversation.application.ConversationQueryService;
import kr.co.deepple.conversation.application.dto.ConversationDetailView;
import kr.co.deepple.conversation.application.dto.ConversationViews;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.CONVERSATION)
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

  private final ConversationQueryService conversationQueryService;

  @Operation(
      summary = "대화 목록 조회",
      description = "메시지 탭. 메시지·프로필 교환을 상대 한 명당 한 줄로 합쳐 마지막 활동이 최근인 순으로 20개씩 준다. 차단한·차단된 상대는 뺀다.")
  @GetMapping
  public ResponseEntity<ConversationViews> getConversations(
      @CurrentMemberId long memberId,
      @Parameter(description = "이전 응답의 nextCursor. 첫 페이지는 비운다") @RequestParam(required = false)
          String cursor) {
    return ResponseEntity.ok(conversationQueryService.findConversations(memberId, cursor));
  }

  @Operation(
      summary = "메시지 상세 조회",
      description = "한 상대와의 프로필 교환·메시지·답장·거절·만료를 시간 순 타임라인으로, 지금 진행 중인 매칭·교환·연락처와 함께 준다.")
  @GetMapping("/{memberId}")
  public ResponseEntity<ConversationDetailView> getConversation(
      @CurrentMemberId long memberId,
      @Parameter(description = "상대 회원 ID") @PathVariable("memberId") long counterpartId) {
    return ResponseEntity.ok(conversationQueryService.getConversation(memberId, counterpartId));
  }
}
