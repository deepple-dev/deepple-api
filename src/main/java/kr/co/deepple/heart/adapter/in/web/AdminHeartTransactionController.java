package kr.co.deepple.heart.adapter.in.web;

import kr.co.deepple.common.security.CurrentAdminId;
import kr.co.deepple.heart.adapter.in.web.dto.AdminHeartGrantRequest;
import kr.co.deepple.heart.application.AdminHeartGrantService;
import kr.co.deepple.heart.application.dto.AdminHeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.AdminHeartTransactionView;
import kr.co.deepple.heart.application.port.out.AdminHeartTransactionQueryRepository;
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
    public ResponseEntity<Page<AdminHeartTransactionView>> getPage(
        @ModelAttribute AdminHeartTransactionSearchCondition condition,
        @PageableDefault(size = 100) Pageable pageable
    ) {
        Page<AdminHeartTransactionView> page = adminHeartTransactionQueryRepository.findPage(condition, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "관리자 하트 지급 API")
    @PostMapping("/grants")
    public ResponseEntity<Void> grant(
        @CurrentAdminId long adminId,
        @Valid @RequestBody AdminHeartGrantRequest request
    ) {
        adminHeartGrantService.grant(adminId, request);
        return ResponseEntity.noContent().build();
    }
}
