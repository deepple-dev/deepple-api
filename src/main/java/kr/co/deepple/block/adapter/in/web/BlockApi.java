package kr.co.deepple.block.adapter.in.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.block.adapter.in.web.dto.BlockCreateRequest;
import kr.co.deepple.block.application.port.in.BlockCommander;
import kr.co.deepple.common.security.CurrentMemberId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "차단 API")
@RestController
@RequestMapping("/blocks")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockApi {

  private final BlockCommander blockCommander;

  @PostMapping
  public ResponseEntity<Void> createBlock(
      @RequestBody @Valid BlockCreateRequest request, @CurrentMemberId long memberId) {
    Long blockerId = memberId;
    blockCommander.createBlock(blockerId, request.blockedId());
    return ResponseEntity.noContent().build();
  }
}
