package deepple.deepple.member.presentation.screening;

import deepple.deepple.admin.command.application.screening.ScreeningService;
import deepple.deepple.auth.presentation.CurrentMemberId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "멤버 심사 API")
@RestController
@RequestMapping("/members/screenings")
@RequiredArgsConstructor
public class MemberScreeningController {

    private final ScreeningService screeningService;

    @Operation(summary = "재심사 요청")
    @PostMapping("/rescreen")
    public ResponseEntity<Void> rescreen(
        @CurrentMemberId long memberId
    ) {
        screeningService.rescreen(memberId);
        return ResponseEntity.noContent().build();
    }
}