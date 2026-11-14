package kr.co.deepple.payment.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.payment.application.dto.RefundSearchCondition;
import kr.co.deepple.payment.application.dto.RefundView;
import kr.co.deepple.payment.application.port.out.RefundQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.PAYMENT)
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/refunds")
public class AdminRefundController {

  private final RefundQueryRepository refundQueryRepository;

  @Operation(summary = "환불 목록 조회")
  @GetMapping
  public ResponseEntity<Page<RefundView>> getPage(
      @ModelAttribute RefundSearchCondition condition,
      @PageableDefault(size = 20) Pageable pageable) {
    Page<RefundView> page = refundQueryRepository.findPage(condition, pageable);
    return ResponseEntity.ok(page);
  }
}
