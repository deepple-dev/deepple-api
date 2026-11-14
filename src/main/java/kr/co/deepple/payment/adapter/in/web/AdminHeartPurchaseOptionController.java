package kr.co.deepple.payment.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.payment.application.HeartPurchaseOptionService;
import kr.co.deepple.payment.application.dto.HeartPurchaseOptionCreateRequest;
import kr.co.deepple.payment.application.dto.HeartPurchaseOptionSearchCondition;
import kr.co.deepple.payment.application.dto.HeartPurchaseOptionView;
import kr.co.deepple.payment.application.port.out.HeartPurchaseOptionQueryRepository;
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

@Tag(name = ApiTags.PAYMENT)
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
  public ResponseEntity<Void> delete(@PathVariable long id) {
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
