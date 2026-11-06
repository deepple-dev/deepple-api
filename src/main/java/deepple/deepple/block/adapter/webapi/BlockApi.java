package deepple.deepple.block.adapter.webapi;

import deepple.deepple.auth.presentation.CurrentMemberId;
import deepple.deepple.block.adapter.webapi.dto.BlockCreateRequest;
import deepple.deepple.block.application.provided.BlockCommander;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
        @RequestBody @Valid BlockCreateRequest request,
        @CurrentMemberId long memberId
    ) {
        Long blockerId = memberId;
        blockCommander.createBlock(blockerId, request.blockedId());
        return ResponseEntity.noContent().build();
    }
}
