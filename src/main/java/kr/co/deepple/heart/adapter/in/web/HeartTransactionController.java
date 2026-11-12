package kr.co.deepple.heart.adapter.in.web;

import kr.co.deepple.heart.application.port.in.FindHeartTransactionsUseCase;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.heart.application.dto.HeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.HeartTransactionViews;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "하트 내역 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/heart-transactions")
public class HeartTransactionController {
    private final FindHeartTransactionsUseCase findHeartTransactionsUseCase;

    @Operation(summary = "하트 내역 조회")
    @GetMapping
    public ResponseEntity<HeartTransactionViews> getHeartTransactions(
        @ModelAttribute HeartTransactionSearchCondition condition,
        @CurrentMemberId long memberId
    ) {
        HeartTransactionViews views = findHeartTransactionsUseCase.findHeartTransactions(memberId, condition);
        return ResponseEntity.ok(views);
    }
}
