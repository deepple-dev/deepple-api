package kr.co.deepple.community.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.community.application.SelfIntroductionQueryService;
import kr.co.deepple.community.application.SelfIntroductionService;
import kr.co.deepple.community.application.dto.AdminSelfIntroductionSearchCondition;
import kr.co.deepple.community.application.dto.AdminSelfIntroductionView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.COMMUNITY)
@RestController
@RequestMapping("/admin/selfintroduction")
@RequiredArgsConstructor
public class AdminSelfIntroductionController {

  private final SelfIntroductionQueryService selfIntroductionQueryService;
  private final SelfIntroductionService selfIntroductionService;

  @Operation(summary = "셀프 소개 목록 조회 API")
  @GetMapping
  public ResponseEntity<Page<AdminSelfIntroductionView>> getSelfIntroductions(
      @ModelAttribute AdminSelfIntroductionSearchCondition condition,
      @PageableDefault(size = 100) Pageable pageable) {
    return ResponseEntity.ok(
        selfIntroductionQueryService.findAdminSelfIntroductions(condition, pageable));
  }

  @Operation(summary = "셀프 소개 공개 전환 API")
  @PatchMapping("/{id}/open")
  public ResponseEntity<Void> updateSelfIntroductionToOpen(@PathVariable long id) {
    selfIntroductionService.changeOpenStatus(id, true);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "셀프 소개 비공개 전환 API")
  @PatchMapping("/{id}/close")
  public ResponseEntity<Void> updateSelfIntroductionToClose(@PathVariable long id) {
    selfIntroductionService.changeOpenStatus(id, false);
    return ResponseEntity.noContent().build();
  }
}
