package deepple.deepple.member.presentation.introduction;

import deepple.deepple.auth.presentation.CurrentMemberId;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.application.introduction.MemberIdealService;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.presentation.introduction.dto.MemberIdealUpdateRequest;
import deepple.deepple.member.query.introduction.application.MemberIdealView;
import deepple.deepple.member.query.introduction.intra.MemberIdealQueryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        MemberIdealView memberIdealView = memberIdealQueryRepository.findMemberIdealByMemberId(memberId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_IDEAL_NOT_FOUND, "memberId=" + memberId));
        return ResponseEntity.ok(memberIdealView);
    }

    @Operation(summary = "멤버 이상형 설정 수정")
    @PatchMapping
    public ResponseEntity<Void> updateIdeal(@Valid @RequestBody MemberIdealUpdateRequest request,
        @CurrentMemberId long memberId) {
        idealService.update(request, memberId);
        return ResponseEntity.noContent().build();
    }
}
