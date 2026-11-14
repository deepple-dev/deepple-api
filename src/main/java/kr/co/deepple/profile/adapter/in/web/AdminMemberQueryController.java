package kr.co.deepple.profile.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.profile.application.AdminMemberQueryService;
import kr.co.deepple.profile.application.dto.AdminMemberDetailView;
import kr.co.deepple.profile.application.dto.AdminMemberSearchCondition;
import kr.co.deepple.profile.application.dto.AdminMemberView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 페이지 멤버 관리 API")
@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberQueryController {

  private final AdminMemberQueryService adminMemberQueryService;

  @Operation(summary = "멤버 목록 조회")
  @GetMapping
  public ResponseEntity<Page<AdminMemberView>> getMembers(
      @Valid @ModelAttribute AdminMemberSearchCondition condition,
      @PageableDefault(size = 100) Pageable pageable) {
    return ResponseEntity.ok(adminMemberQueryService.findMembers(condition, pageable));
  }

  @Operation(summary = "멤버 상세 조회")
  @GetMapping("/{memberId}")
  public ResponseEntity<AdminMemberDetailView> getMemberDetail(@PathVariable long memberId) {
    return ResponseEntity.ok(adminMemberQueryService.getMemberDetail(memberId));
  }
}
