package deepple.deepple.community.presentation.profileexchange;

import deepple.deepple.auth.presentation.CurrentMemberId;
import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.community.command.application.profileexchange.ProfileExchangeService;
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
    public ResponseEntity<BaseResponse<Void>> requestProfileExchange(@CurrentMemberId long memberId,
        @PathVariable @Min(1) Long responderId) {
        profileExchangeService.request(memberId, responderId);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "프로필 교환 수락 API")
    @PatchMapping("/{profileExchangeId}/approve")
    public ResponseEntity<BaseResponse<Void>> approveProfileExchange(@CurrentMemberId long memberId,
        @PathVariable @Min(1) Long profileExchangeId) {
        profileExchangeService.approve(profileExchangeId, memberId);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "프로필 교환 거절 API")
    @PatchMapping("/{profileExchangeId}/reject")
    public ResponseEntity<BaseResponse<Void>> rejectProfileExchange(@CurrentMemberId long memberId,
        @PathVariable @Min(1) Long profileExchangeId) {
        profileExchangeService.reject(profileExchangeId, memberId);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }
}
