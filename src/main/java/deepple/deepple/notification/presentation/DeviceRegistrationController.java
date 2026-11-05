package deepple.deepple.notification.presentation;

import deepple.deepple.auth.presentation.CurrentMemberId;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.notification.command.application.DeviceRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static deepple.deepple.common.enums.StatusType.OK;

@Tag(name = "기기 설정 API")
@RestController
@RequestMapping("/notifications/device-registration")
@RequiredArgsConstructor
public class DeviceRegistrationController {

    private final DeviceRegistrationService deviceRegistrationService;

    @Operation(summary = "기기 등록 및 업데이트")
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> register(
        @Valid @RequestBody DeviceRegisterRequest request,
        @CurrentMemberId long memberId
    ) {
        deviceRegistrationService.register(memberId, request);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }
}
