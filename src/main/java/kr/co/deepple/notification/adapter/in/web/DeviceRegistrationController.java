package kr.co.deepple.notification.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.notification.application.DeviceRegistrationService;
import kr.co.deepple.notification.application.dto.DeviceRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.NOTIFICATION)
@RestController
@RequestMapping("/notifications/device-registration")
@RequiredArgsConstructor
public class DeviceRegistrationController {

  private final DeviceRegistrationService deviceRegistrationService;

  @Operation(summary = "기기 등록 및 업데이트")
  @PostMapping
  public ResponseEntity<Void> register(
      @Valid @RequestBody DeviceRegisterRequest request, @CurrentMemberId long memberId) {
    deviceRegistrationService.register(memberId, request);
    return ResponseEntity.noContent().build();
  }
}
