package kr.co.deepple.profile.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.profile.application.MemberProfileQueryService;
import kr.co.deepple.profile.application.dto.MemberInfoView;
import kr.co.deepple.profile.application.dto.MemberProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.MEMBER)
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberProfileQueryController {

  private final MemberProfileQueryService memberProfileQueryService;

  @Operation(summary = "회원(자기 자신) 정보 캐싱용 API")
  @GetMapping("/cache")
  public ResponseEntity<MemberInfoView> getMyInfoCache(@CurrentMemberId long memberId) {
    return ResponseEntity.ok(memberProfileQueryService.getInfoCache(memberId));
  }

  @Operation(summary = "상대방 프로필 조회 API")
  @GetMapping("/{memberId}")
  public ResponseEntity<MemberProfileResponse> getOtherProfile(
      @CurrentMemberId long memberId, @PathVariable("memberId") long otherMemberId) {
    return ResponseEntity.ok(memberProfileQueryService.getMemberProfile(memberId, otherMemberId));
  }
}
