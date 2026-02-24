package deepple.deepple.member.presentation.member;

import deepple.deepple.auth.presentation.AuthContext;
import deepple.deepple.auth.presentation.AuthPrincipal;
import deepple.deepple.auth.presentation.RefreshTokenCookieProperties;
import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.member.command.application.member.MemberAuthService;
import deepple.deepple.member.command.application.member.dto.MemberLoginServiceDto;
import deepple.deepple.member.command.application.member.dto.TokenPairResponse;
import deepple.deepple.member.presentation.member.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "멤버 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberAuthController {

    private final RefreshTokenCookieProperties refreshTokenCookieProperties;
    private final MemberAuthService memberAuthService;

    @Operation(summary = "멤버 로그인 및 회원가입")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<MemberLoginResponse>> login(@Valid @RequestBody MemberLoginRequest request) {
        MemberLoginServiceDto loginServiceDto = memberAuthService.login(request.phoneNumber(), request.code());

        HttpHeaders headers = new HttpHeaders();
        ResponseCookie refreshTokenCookie = getResponseCookieCreatedRefreshToken(loginServiceDto.refreshToken());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        MemberLoginResponse loginResponse = MemberDtoMapper.toMemberLoginResponse(loginServiceDto);

        return ResponseEntity.ok()
            .headers(headers)
            .body(BaseResponse.of(StatusType.OK, loginResponse));
    }

    @Operation(summary = "멤버 로그인 (테스트용, 인증번호X)")
    @PostMapping("/login/test")
    public ResponseEntity<BaseResponse<MemberLoginResponse>> test(@Valid @RequestBody MemberTestLoginRequest request) {
        MemberLoginServiceDto loginServiceDto = memberAuthService.test(request.phoneNumber());

        HttpHeaders headers = new HttpHeaders();
        ResponseCookie refreshTokenCookie = getResponseCookieCreatedRefreshToken(loginServiceDto.refreshToken());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        MemberLoginResponse loginResponse = MemberDtoMapper.toMemberLoginResponse(loginServiceDto);

        return ResponseEntity.ok()
            .headers(headers)
            .body(BaseResponse.of(StatusType.OK, loginResponse));
    }

    @Operation(summary = "토큰 갱신")
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenRefreshResponse>> refresh(
        @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        TokenPairResponse tokenPair = memberAuthService.refresh(refreshToken);

        HttpHeaders headers = new HttpHeaders();
        ResponseCookie refreshTokenCookie = getResponseCookieCreatedRefreshToken(tokenPair.refreshToken());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        TokenRefreshResponse response = new TokenRefreshResponse(tokenPair.accessToken());

        return ResponseEntity.ok()
            .headers(headers)
            .body(BaseResponse.of(StatusType.OK, response));
    }

    @Operation(summary = "멤버 로그아웃")
    @GetMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
        @AuthPrincipal AuthContext authContext,
        @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        memberAuthService.logout(authContext.getId(), refreshToken);

        HttpHeaders headers = new HttpHeaders();
        ResponseCookie deleteCookie = getResponseCookieDeletedRefreshToken();
        headers.add(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok()
            .headers(headers)
            .body(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "멤버 탈퇴")
    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> delete(
        @CookieValue(value = "refresh_token", required = false) String refreshToken,
        @AuthPrincipal AuthContext authContext) {
        memberAuthService.delete(authContext.getId(), refreshToken);

        HttpHeaders headers = new HttpHeaders();
        ResponseCookie deleteCookie = getResponseCookieDeletedRefreshToken();
        headers.add(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok()
            .headers(headers)
            .body(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "휴대폰 번호 인증 코드 발송")
    @PostMapping("/code")
    public ResponseEntity<BaseResponse<Void>> getCode(@RequestBody @Valid MemberCodeRequest request) {
        memberAuthService.sendAuthCode(request.phoneNumber());
        return ResponseEntity.ok()
            .body(BaseResponse.from(StatusType.OK));
    }

    private ResponseCookie getResponseCookieDeletedRefreshToken() {
        return ResponseCookie.from(refreshTokenCookieProperties.name(), "")
            .httpOnly(refreshTokenCookieProperties.httpOnly())
            .secure(refreshTokenCookieProperties.secure())
            .sameSite(refreshTokenCookieProperties.sameSite())
            .path(refreshTokenCookieProperties.path())
            .maxAge(0)
            .build();
    }

    private ResponseCookie getResponseCookieCreatedRefreshToken(String refreshToken) {
        return ResponseCookie.from(refreshTokenCookieProperties.name(), refreshToken)
            .httpOnly(refreshTokenCookieProperties.httpOnly())
            .secure(refreshTokenCookieProperties.secure())
            .sameSite(refreshTokenCookieProperties.sameSite())
            .path(refreshTokenCookieProperties.path())
            .maxAge(refreshTokenCookieProperties.maxAge())
            .build();
    }
}