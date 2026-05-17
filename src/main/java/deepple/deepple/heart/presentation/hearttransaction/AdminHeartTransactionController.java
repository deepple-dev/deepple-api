package deepple.deepple.heart.presentation.hearttransaction;

import deepple.deepple.auth.presentation.AuthContext;
import deepple.deepple.auth.presentation.AuthPrincipal;
import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.heart.command.application.admingrant.AdminHeartGrantService;
import deepple.deepple.heart.query.hearttransaction.AdminHeartTransactionQueryRepository;
import deepple.deepple.heart.query.hearttransaction.condition.AdminHeartTransactionSearchCondition;
import deepple.deepple.heart.query.hearttransaction.view.AdminHeartTransactionView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "어드민 하트 트랜잭션 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/heart-transactions")
public class AdminHeartTransactionController {
    private final AdminHeartTransactionQueryRepository adminHeartTransactionQueryRepository;
    private final AdminHeartGrantService adminHeartGrantService;

    @Operation(summary = "하트 트랜잭션 목록 조회 API")
    @GetMapping
    public ResponseEntity<BaseResponse<Page<AdminHeartTransactionView>>> getPage(
        @ModelAttribute AdminHeartTransactionSearchCondition condition,
        @PageableDefault(size = 100) Pageable pageable
    ) {
        Page<AdminHeartTransactionView> page = adminHeartTransactionQueryRepository.findPage(condition, pageable);
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK, page));
    }

    @Operation(summary = "관리자 하트 지급 API")
    @PostMapping("/grants")
    public ResponseEntity<BaseResponse<Void>> grant(
        @AuthPrincipal AuthContext authContext,
        @Valid @RequestBody AdminHeartGrantRequest request
    ) {
        adminHeartGrantService.grant(authContext.getId(), request);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }
}
