package kr.co.deepple.community.presentation.profileexchange;

import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.community.command.application.profileexchange.ProfileExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "프로필 교환 요청 및 응답 API")
@RestController
@RequestMapping("/profile-exchange")
@RequiredArgsConstructor
public class ProfileExchangeController {
    private final ProfileExchangeService profileExchangeService;

    @Operation(summary = "프로필 교환 신청 API")
    @PostMapping("/request/{responderId}")
    public ResponseEntity<Void> requestProfileExchange(@CurrentMemberId long memberId,
        @PathVariable @Min(1) Long responderId) {
        profileExchangeService.request(memberId, responderId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "프로필 교환 수락 API")
    @PatchMapping("/{profileExchangeId}/approve")
    public ResponseEntity<Void> approveProfileExchange(@CurrentMemberId long memberId,
        @PathVariable @Min(1) Long profileExchangeId) {
        profileExchangeService.approve(profileExchangeId, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "프로필 교환 거절 API")
    @PatchMapping("/{profileExchangeId}/reject")
    public ResponseEntity<Void> rejectProfileExchange(@CurrentMemberId long memberId,
        @PathVariable @Min(1) Long profileExchangeId) {
        profileExchangeService.reject(profileExchangeId, memberId);
        return ResponseEntity.noContent().build();
    }
}
