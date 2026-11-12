package kr.co.deepple.payment.presentation.refund;

import kr.co.deepple.payment.query.refund.RefundQueryRepository;
import kr.co.deepple.payment.query.refund.condition.RefundSearchCondition;
import kr.co.deepple.payment.query.refund.view.RefundView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "어드민 환불 관리 API")
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