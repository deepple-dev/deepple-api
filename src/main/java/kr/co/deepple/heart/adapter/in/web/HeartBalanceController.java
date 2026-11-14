package kr.co.deepple.heart.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.heart.application.dto.HeartBalanceView;
import kr.co.deepple.heart.application.port.in.GetHeartBalanceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.HEART)
@RestController
@RequiredArgsConstructor
public class HeartBalanceController {

  private final GetHeartBalanceUseCase getHeartBalanceUseCase;

  @Operation(summary = "보유 하트 조회 API")
  @GetMapping("/member/heartbalance")
  public ResponseEntity<HeartBalanceView> getHeartBalance(@CurrentMemberId long memberId) {
    return ResponseEntity.ok(getHeartBalanceUseCase.getHeartBalance(memberId));
  }
}
