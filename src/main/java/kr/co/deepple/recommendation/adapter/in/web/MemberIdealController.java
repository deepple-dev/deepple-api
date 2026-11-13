package kr.co.deepple.recommendation.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.recommendation.adapter.out.persistence.MemberIdealQueryRepository;
import kr.co.deepple.recommendation.application.MemberIdealService;
import kr.co.deepple.recommendation.application.dto.MemberIdealUpdateRequest;
import kr.co.deepple.recommendation.application.dto.MemberIdealView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "멤버 이상형 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member/ideal")
public class MemberIdealController {

  private final MemberIdealService idealService;
  private final MemberIdealQueryRepository memberIdealQueryRepository;

  @Operation(summary = "멤버 이상형 설정 조회")
  @GetMapping
  public ResponseEntity<MemberIdealView> getMemberIdeal(@CurrentMemberId long memberId) {
    MemberIdealView memberIdealView =
        memberIdealQueryRepository
            .findMemberIdealByMemberId(memberId)
            .orElseThrow(() ->
                new DeeppleException(
                    MemberErrorCode.MEMBER_IDEAL_NOT_FOUND, "memberId=" + memberId));
    return ResponseEntity.ok(memberIdealView);
  }

  @Operation(summary = "멤버 이상형 설정 수정")
  @PatchMapping
  public ResponseEntity<Void> updateIdeal(
      @Valid @RequestBody MemberIdealUpdateRequest request, @CurrentMemberId long memberId) {
    idealService.update(request, memberId);
    return ResponseEntity.noContent().build();
  }
}
