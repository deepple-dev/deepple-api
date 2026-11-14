package kr.co.deepple.auth.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.auth.application.AdminAuthService;
import kr.co.deepple.auth.application.dto.AdminLoginRequest;
import kr.co.deepple.auth.application.dto.AdminLoginResponse;
import kr.co.deepple.auth.application.dto.AdminRefreshResponse;
import kr.co.deepple.auth.application.dto.AdminSignupRequest;
import kr.co.deepple.auth.application.dto.AdminSignupResponse;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.RefreshTokenCookies;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.AUTH)
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {

  private static final String BEARER_PREFIX = "Bearer ";

  private final RefreshTokenCookies refreshTokenCookies;
  private final AdminAuthService adminAuthService;

  @Operation(summary = "관리자 회원가입")
  @PostMapping("/signup")
  public ResponseEntity<AdminSignupResponse> signup(
      @Valid @RequestBody AdminSignupRequest request) {
    AdminSignupResponse data = adminAuthService.signup(request);
    return ResponseEntity.ok(data);
  }

  @Operation(summary = "관리자 로그인")
  @PostMapping("/login")
  public ResponseEntity<Void> login(@Valid @RequestBody AdminLoginRequest request) {
    AdminLoginResponse response = adminAuthService.login(request);

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + response.accessToken());
    headers.add(
        HttpHeaders.SET_COOKIE, refreshTokenCookies.create(response.refreshToken()).toString());

    return ResponseEntity.noContent().headers(headers).build();
  }

  @Operation(summary = "관리자 토큰 재발급")
  @PostMapping("/refresh")
  public ResponseEntity<AdminRefreshResponse> refresh(
      @CookieValue(value = "refresh_token", required = false) String refreshToken) {
    AdminLoginResponse response = adminAuthService.refresh(refreshToken);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.SET_COOKIE, refreshTokenCookies.create(response.refreshToken()).toString())
        .body(new AdminRefreshResponse(response.accessToken()));
  }

  @Operation(summary = "관리자 로그아웃")
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(value = "refresh_token", required = false) String refreshToken) {
    adminAuthService.logout(refreshToken);

    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookies.expire().toString())
        .build();
  }
}
