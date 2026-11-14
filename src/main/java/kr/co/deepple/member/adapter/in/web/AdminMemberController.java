package kr.co.deepple.member.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.member.application.AdminMemberService;
import kr.co.deepple.member.application.dto.AdminMemberSettingUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.MEMBER)
@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

  private final AdminMemberService adminMemberService;

  @Operation(summary = "멤버 설정 업데이트")
  @PutMapping("/{memberId}/settings")
  public ResponseEntity<Void> updateMemberSetting(
      @Valid @RequestBody AdminMemberSettingUpdateRequest request, @PathVariable long memberId) {
    adminMemberService.updateMemberSetting(memberId, request);
    return ResponseEntity.noContent().build();
  }
}
