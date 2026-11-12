package kr.co.deepple.member.presentation.member;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.application.member.AdminMemberService;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.presentation.member.dto.AdminMemberSettingUpdateRequest;
import kr.co.deepple.member.query.member.condition.AdminMemberSearchCondition;
import kr.co.deepple.member.query.member.infra.AdminMemberQueryRepository;
import kr.co.deepple.member.query.member.view.AdminMemberDetailView;
import kr.co.deepple.member.query.member.view.AdminMemberView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "관리자 페이지 멤버 관리 API")
@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminMemberService;
    private final AdminMemberQueryRepository adminMemberQueryRepository;

    @Operation(summary = "멤버 목록 조회")
    @GetMapping
    public ResponseEntity<Page<AdminMemberView>> getMembers(
        @Valid @ModelAttribute AdminMemberSearchCondition condition,
        @PageableDefault(size = 100) Pageable pageable
    ) {
        return ResponseEntity.ok(adminMemberQueryRepository.findMembers(condition, pageable));
    }

    @Operation(summary = "멤버 상세 조회")
    @GetMapping("/{memberId}")
    public ResponseEntity<AdminMemberDetailView> getMemberDetail(@PathVariable long memberId) {
        AdminMemberDetailView view = adminMemberQueryRepository.findById(memberId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
        return ResponseEntity.ok(view);
    }

    @Operation(summary = "멤버 설정 업데이트")
    @PutMapping("/{memberId}/settings")
    public ResponseEntity<Void> updateMemberSetting(
        @Valid @RequestBody AdminMemberSettingUpdateRequest request,
        @PathVariable long memberId
    ) {
        adminMemberService.updateMemberSetting(memberId, request);
        return ResponseEntity.noContent().build();
    }
}
