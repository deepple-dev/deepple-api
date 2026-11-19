package kr.co.deepple.profile.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.profile.application.dto.MemberSummaryView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.MEMBER)
@RestController
public class MemberSummaryController {

  @Operation(
      summary = "프로필 바텀시트 조회",
      description =
          "피드·댓글 작성자의 프로필을 누르면 뜨는 바텀시트용. 차단 관계이거나 상대가 활동 중이 아니면 PROFILE_ACCESS_DENIED로 거절한다. "
              + "구현 전이라 고정된 예시 값을 응답한다.")
  @GetMapping("/members/{memberId}/summary")
  public ResponseEntity<MemberSummaryView> getMemberSummary(
      @CurrentMemberId long currentMemberId, @PathVariable long memberId) {
    return ResponseEntity.ok(
        new MemberSummaryView(
            memberId,
            "한강러너",
            "DECISIVE_INDEPENDENT",
            "자기주도형",
            "https://example.com/profiles/102.webp",
            32,
            "MALE",
            "SEOUL",
            "DONGJAK_GU",
            "ISTP",
            Set.of("HIKING_AND_CLIMBING", "PERFORMANCE_AND_EXHIBITION"),
            null,
            null));
  }
}
