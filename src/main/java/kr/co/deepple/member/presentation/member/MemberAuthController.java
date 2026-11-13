package kr.co.deepple.member.presentation.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.auth.adapter.in.web.RefreshTokenCookies;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.member.command.application.member.MemberAuthService;
import kr.co.deepple.member.command.application.member.dto.MemberLoginServiceDto;
import kr.co.deepple.member.command.application.member.dto.TokenPairResponse;
import kr.co.deepple.member.presentation.member.dto.MemberCodeRequest;
import kr.co.deepple.member.presentation.member.dto.MemberLoginRequest;
import kr.co.deepple.member.presentation.member.dto.MemberLoginResponse;
import kr.co.deepple.member.presentation.member.dto.TokenRefreshResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
  private final MemberAuthService memberAuthService;

  @Operation(summary = "멤버 로그인 및 회원가입")
  @PostMapping("/login")
  public ResponseEntity<MemberLoginResponse> login(@Valid @RequestBody MemberLoginRequest request) {
    MemberLoginServiceDto loginServiceDto =
        memberAuthService.login(request.phoneNumber(), request.code());

    HttpHeaders headers = new HttpHeaders();
    ResponseCookie refreshTokenCookie = refreshTokenCookies.create(loginServiceDto.refreshToken());
    headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

    MemberLoginResponse loginResponse = MemberDtoMapper.toMemberLoginResponse(loginServiceDto);

    return ResponseEntity.ok().headers(headers).body(loginResponse);
  }

  @Operation(summary = "토큰 갱신")
  @PostMapping("/refresh")
  public ResponseEntity<TokenRefreshResponse> refresh(
      @CookieValue(value = "refresh_token", required = false) String refreshToken) {
    TokenPairResponse tokenPair = memberAuthService.refresh(refreshToken);

    HttpHeaders headers = new HttpHeaders();
    ResponseCookie refreshTokenCookie = refreshTokenCookies.create(tokenPair.refreshToken());
    headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

    TokenRefreshResponse response = new TokenRefreshResponse(tokenPair.accessToken());

    return ResponseEntity.ok().headers(headers).body(response);
  }

  @Operation(summary = "멤버 로그아웃")
  @GetMapping("/logout")
  public ResponseEntity<Void> logout(
      @CurrentMemberId long memberId,
      @CookieValue(value = "refresh_token", required = false) String refreshToken) {
    memberAuthService.logout(memberId, refreshToken);

    HttpHeaders headers = new HttpHeaders();
    ResponseCookie deleteCookie = refreshTokenCookies.expire();
    headers.add(HttpHeaders.SET_COOKIE, deleteCookie.toString());

    return ResponseEntity.noContent().headers(headers).build();
  }

  @Operation(summary = "멤버 탈퇴")
  @DeleteMapping
  public ResponseEntity<Void> delete(@CurrentMemberId long memberId) {
    memberAuthService.delete(memberId);

    HttpHeaders headers = new HttpHeaders();
    ResponseCookie deleteCookie = refreshTokenCookies.expire();
    headers.add(HttpHeaders.SET_COOKIE, deleteCookie.toString());

    return ResponseEntity.noContent().headers(headers).build();
  }

  @Operation(summary = "휴대폰 번호 인증 코드 발송")
  @PostMapping("/code")
  public ResponseEntity<Void> getCode(@RequestBody @Valid MemberCodeRequest request) {
    memberAuthService.sendAuthCode(request.phoneNumber());
    return ResponseEntity.noContent().build();
  }
}
