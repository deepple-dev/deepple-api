package kr.co.deepple.member.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.deepple.auth.application.port.in.LogoutMemberUseCase;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.common.security.RefreshTokenCookies;
import kr.co.deepple.member.application.MemberContactService;
import kr.co.deepple.member.application.MemberMapper;
import kr.co.deepple.member.application.MemberProfileService;
import kr.co.deepple.member.application.MemberQueryService;
import kr.co.deepple.member.application.dto.MemberContactView;
import kr.co.deepple.member.application.dto.MemberMyProfileResponse;
import kr.co.deepple.member.application.dto.MemberProfileUpdateRequest;
import kr.co.deepple.member.application.dto.MemberStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 정보 조회 및 변경 API")
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

  private final MemberContactService memberContactService;
  private final MemberProfileService memberProfileService;
  private final MemberQueryService memberQueryService;
  private final LogoutMemberUseCase logoutMemberUseCase;
  private final RefreshTokenCookies refreshTokenCookies;

  @Operation(summary = "프로필 정보 업데이트 API")
  @PutMapping("/profile")
  public ResponseEntity<Void> updateProfile(
      @RequestBody MemberProfileUpdateRequest request, @CurrentMemberId long memberId) {
    memberProfileService.updateMember(memberId, request);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "회원(자기 자신) 상태 조회 API")
  @GetMapping("/status")
  public ResponseEntity<MemberStatusResponse> getMyStatus(@CurrentMemberId long memberId) {
    return ResponseEntity.ok(memberProfileService.getStatus(memberId));
  }

  @Operation(summary = "회원(자기 자신) 프로필 정보 조회 API")
  @GetMapping("/profile")
  public ResponseEntity<MemberMyProfileResponse> getMyProfile(@CurrentMemberId long memberId) {
    return ResponseEntity.ok(
        MemberMapper.toMemberMyProfileResponse(memberQueryService.getProfile(memberId)));
  }

  @Operation(summary = "휴면 계정 전환 API")
  @PostMapping("/profile/dormant")
  public ResponseEntity<Void> deactivate(
      @CurrentMemberId long memberId,
      @CookieValue(value = "refresh_token", required = false) String refreshToken) {
    memberProfileService.deactivate(memberId);
    logoutMemberUseCase.logout(memberId, refreshToken);

    HttpHeaders headers = new HttpHeaders();
    ResponseCookie deleteCookie = refreshTokenCookies.expire();
    headers.add(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    return ResponseEntity.noContent().headers(headers).build();
  }

  @Operation(summary = "활동 계정 전환 API")
  @PostMapping("/profile/active")
  public ResponseEntity<Void> reactivate(@CurrentMemberId long memberId) {
    memberProfileService.reactivate(memberId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "연락처(자기 자신) 정보 조회 API")
  @GetMapping("/profile/contact")
  public ResponseEntity<MemberContactView> getMyContact(@CurrentMemberId long memberId) {
    return ResponseEntity.ok(memberQueryService.getContact(memberId));
  }

  @Operation(summary = "주요 연락처 수단(Kakao) 변경 API")
  @PatchMapping("/profile/contact/kakao")
  public ResponseEntity<Void> updateKakaoId(
      @CurrentMemberId long memberId, @RequestBody String kakaoId) {
    memberContactService.updateKakaoId(memberId, kakaoId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "주요 연락처 수단 변경(Phone) API")
  @PatchMapping("/profile/contact/phone")
  public ResponseEntity<Void> updatePhoneNumber(
      @CurrentMemberId long memberId, @RequestBody String phone) {
    memberContactService.updatePhoneNumber(memberId, phone);
    return ResponseEntity.noContent().build();
  }
}
