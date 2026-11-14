package kr.co.deepple.auth.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.auth.application.dto.MemberCodeRequest;
import kr.co.deepple.auth.application.dto.MemberLoginRequest;
import kr.co.deepple.auth.application.dto.MemberLoginResponse;
import kr.co.deepple.auth.application.dto.TokenPairResponse;
import kr.co.deepple.auth.application.dto.TokenRefreshResponse;
import kr.co.deepple.auth.application.port.in.LoginMemberUseCase;
import kr.co.deepple.auth.application.port.in.LogoutMemberUseCase;
import kr.co.deepple.auth.application.port.in.RefreshMemberTokenUseCase;
import kr.co.deepple.auth.application.port.in.SendAuthCodeUseCase;
import kr.co.deepple.auth.application.port.in.WithdrawMemberUseCase;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.common.security.RefreshTokenCookies;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "멤버 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberAuthController {

  private final RefreshTokenCookies refreshTokenCookies;
  private final SendAuthCodeUseCase sendAuthCodeUseCase;
  private final LoginMemberUseCase loginMemberUseCase;
  private final RefreshMemberTokenUseCase refreshMemberTokenUseCase;
  private final LogoutMemberUseCase logoutMemberUseCase;
  private final WithdrawMemberUseCase withdrawMemberUseCase;

  @Operation(summary = "휴대폰 번호 인증 코드 발송")
  @PostMapping("/code")
  public ResponseEntity<Void> getCode(@RequestBody @Valid MemberCodeRequest request) {
    sendAuthCodeUseCase.sendAuthCode(request);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "멤버 로그인 및 회원가입")
  @PostMapping("/login")
  public ResponseEntity<MemberLoginResponse> login(@Valid @RequestBody MemberLoginRequest request) {
    TokenPairResponse tokenPair = loginMemberUseCase.login(request);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.SET_COOKIE, refreshTokenCookies.create(tokenPair.refreshToken()).toString())
        .body(new MemberLoginResponse(tokenPair.accessToken()));
  }

  @Operation(summary = "토큰 갱신")
  @PostMapping("/refresh")
  public ResponseEntity<TokenRefreshResponse> refresh(
      @CookieValue(value = "refresh_token", required = false) String refreshToken) {
    TokenPairResponse tokenPair = refreshMemberTokenUseCase.refresh(refreshToken);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.SET_COOKIE, refreshTokenCookies.create(tokenPair.refreshToken()).toString())
        .body(new TokenRefreshResponse(tokenPair.accessToken()));
  }

  @Operation(summary = "멤버 로그아웃")
  @GetMapping("/logout")
  public ResponseEntity<Void> logout(
      @CurrentMemberId long memberId,
      @CookieValue(value = "refresh_token", required = false) String refreshToken) {
    logoutMemberUseCase.logout(memberId, refreshToken);

    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookies.expire().toString())
        .build();
  }

  @Operation(summary = "멤버 탈퇴")
  @DeleteMapping
  public ResponseEntity<Void> withdraw(@CurrentMemberId long memberId) {
    withdrawMemberUseCase.withdraw(memberId);

    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookies.expire().toString())
        .build();
  }
}
