package kr.co.deepple.member.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.adapter.out.persistence.AdminMemberQueryRepository;
import kr.co.deepple.member.application.AdminMemberService;
import kr.co.deepple.member.application.dto.AdminMemberDetailView;
import kr.co.deepple.member.application.dto.AdminMemberSearchCondition;
import kr.co.deepple.member.application.dto.AdminMemberSettingUpdateRequest;
import kr.co.deepple.member.application.dto.AdminMemberView;
import kr.co.deepple.member.domain.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
      @PageableDefault(size = 100) Pageable pageable) {
    return ResponseEntity.ok(adminMemberQueryRepository.findMembers(condition, pageable));
  }

  @Operation(summary = "멤버 상세 조회")
  @GetMapping("/{memberId}")
  public ResponseEntity<AdminMemberDetailView> getMemberDetail(@PathVariable long memberId) {
    AdminMemberDetailView view =
        adminMemberQueryRepository
            .findById(memberId)
            .orElseThrow(() ->
                new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
    return ResponseEntity.ok(view);
  }

  @Operation(summary = "멤버 설정 업데이트")
  @PutMapping("/{memberId}/settings")
  public ResponseEntity<Void> updateMemberSetting(
      @Valid @RequestBody AdminMemberSettingUpdateRequest request, @PathVariable long memberId) {
    adminMemberService.updateMemberSetting(memberId, request);
    return ResponseEntity.noContent().build();
  }
}
