package kr.co.deepple.payment.presentation.heartpurchaseoption;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.payment.command.application.heartpurchaseoption.HeartPurchaseOptionService;
import kr.co.deepple.payment.presentation.heartpurchaseoption.dto.HeartPurchaseOptionCreateRequest;
import kr.co.deepple.payment.query.heartpurchaseoption.HeartPurchaseOptionQueryRepository;
import kr.co.deepple.payment.query.heartpurchaseoption.condition.HeartPurchaseOptionSearchCondition;
import kr.co.deepple.payment.query.heartpurchaseoption.view.HeartPurchaseOptionView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "어드민 하트 구매 옵션 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/heart-purchase-options")
public class AdminHeartPurchaseOptionController {

  private final HeartPurchaseOptionService heartPurchaseOptionService;
  private final HeartPurchaseOptionQueryRepository heartPurchaseOptionQueryRepository;

  @Operation(summary = "하트 구매 옵션 생성")
  @PostMapping
  public ResponseEntity<Void> create(@Valid @RequestBody HeartPurchaseOptionCreateRequest request) {
    heartPurchaseOptionService.create(request);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "하트 구매 옵션 삭제")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    heartPurchaseOptionService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "하트 구매 옵션 목록 조회")
  @GetMapping
  public ResponseEntity<Page<HeartPurchaseOptionView>> getPage(
      @ModelAttribute HeartPurchaseOptionSearchCondition condition,
      @PageableDefault(size = 100) Pageable pageable) {
    Page<HeartPurchaseOptionView> page =
        heartPurchaseOptionQueryRepository.findPage(condition, pageable);
    return ResponseEntity.ok(page);
  }
}
