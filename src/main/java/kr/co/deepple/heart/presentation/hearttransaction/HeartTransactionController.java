package kr.co.deepple.heart.presentation.hearttransaction;

import kr.co.deepple.auth.presentation.CurrentMemberId;
import kr.co.deepple.heart.query.hearttransaction.HeartTransactionQueryService;
import kr.co.deepple.heart.query.hearttransaction.condition.HeartTransactionSearchCondition;
import kr.co.deepple.heart.query.hearttransaction.view.HeartTransactionViews;
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
    private final HeartTransactionQueryService heartTransactionQueryService;

    @Operation(summary = "하트 내역 조회")
    @GetMapping
    public ResponseEntity<HeartTransactionViews> getHeartTransactions(
        @ModelAttribute HeartTransactionSearchCondition condition,
        @CurrentMemberId long memberId
    ) {
        final HeartTransactionViews views = heartTransactionQueryService.findHeartTransactions(
            memberId, condition);
        return ResponseEntity.ok(views);
    }
}
