package deepple.deepple.member.presentation.member;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.member.command.application.member.AdminMemberService;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.presentation.member.dto.AdminMemberSettingUpdateRequest;
import deepple.deepple.member.query.member.condition.AdminMemberSearchCondition;
import deepple.deepple.member.query.member.infra.AdminMemberQueryRepository;
import deepple.deepple.member.query.member.view.AdminMemberDetailView;
import deepple.deepple.member.query.member.view.AdminMemberView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static deepple.deepple.common.enums.StatusType.OK;

@Tag(name = "관리자 페이지 멤버 관리 API")
@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminMemberService;
    private final AdminMemberQueryRepository adminMemberQueryRepository;

    @Operation(summary = "멤버 목록 조회")
    @GetMapping
    public ResponseEntity<BaseResponse<Page<AdminMemberView>>> getMembers(
        @Valid @ModelAttribute AdminMemberSearchCondition condition,
        @PageableDefault(size = 100) Pageable pageable
    ) {
        return ResponseEntity.ok(BaseResponse.of(OK, adminMemberQueryRepository.findMembers(condition, pageable)));
    }

    @Operation(summary = "멤버 상세 조회")
    @GetMapping("/{memberId}")
    public ResponseEntity<BaseResponse<AdminMemberDetailView>> getMemberDetail(@PathVariable long memberId) {
        AdminMemberDetailView view = adminMemberQueryRepository.findById(memberId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
        return ResponseEntity.ok(BaseResponse.of(OK, view));
    }

    @Operation(summary = "멤버 설정 업데이트")
    @PutMapping("/{memberId}/settings")
    public ResponseEntity<BaseResponse<Void>> updateMemberSetting(
        @Valid @RequestBody AdminMemberSettingUpdateRequest request,
        @PathVariable long memberId
    ) {
        adminMemberService.updateMemberSetting(memberId, request);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }
}
