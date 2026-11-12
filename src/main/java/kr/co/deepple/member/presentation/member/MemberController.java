package kr.co.deepple.member.presentation.member;

import kr.co.deepple.auth.presentation.RefreshTokenCookies;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.member.command.application.member.MemberAuthService;
import kr.co.deepple.member.command.application.member.MemberContactService;
import kr.co.deepple.member.command.application.member.MemberProfileService;
import kr.co.deepple.member.presentation.member.dto.MemberMyProfileResponse;
import kr.co.deepple.member.presentation.member.dto.MemberProfileResponse;
import kr.co.deepple.member.presentation.member.dto.MemberProfileUpdateRequest;
import kr.co.deepple.member.query.member.application.MemberQueryService;
import kr.co.deepple.member.query.member.view.MemberContactView;
import kr.co.deepple.member.query.member.view.MemberInfoView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 정보 조회 및 변경 API")
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberContactService memberContactService;
    private final MemberProfileService memberProfileService;
    private final MemberQueryService memberQueryService;
    private final MemberAuthService memberAuthService;

    private final RefreshTokenCookies refreshTokenCookies;

    @Operation(summary = "프로필 정보 업데이트 API")
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody MemberProfileUpdateRequest request,
        @CurrentMemberId long memberId) {
        memberProfileService.updateMember(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원(자기 자신) 정보 캐싱용 API")
    @GetMapping("/cache")
    public ResponseEntity<MemberInfoView> getMyInfoCache(@CurrentMemberId long memberId) {
        return ResponseEntity.ok(memberQueryService.getInfoCache(memberId));
    }

    @Operation(summary = "회원(자기 자신) 프로필 정보 조회 API")
    @GetMapping("/profile")
    public ResponseEntity<MemberMyProfileResponse> getMyProfile(@CurrentMemberId long memberId) {
        return ResponseEntity.ok(MemberMapper.toMemberMyProfileResponse(memberQueryService.getProfile(memberId)));
    }

    @Operation(summary = "상대방 프로필 조회 API")
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberProfileResponse> getOtherProfile(@CurrentMemberId long memberId,
        @PathVariable("memberId") long otherMemberId) {
        return ResponseEntity.ok(memberQueryService.getMemberProfile(memberId, otherMemberId));
    }

    @Operation(summary = "휴면 계정 전환 API")
    @PostMapping("/profile/dormant")
    public ResponseEntity<Void> deactivate(
        @CurrentMemberId long memberId,
        @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        memberProfileService.deactivate(memberId);
        memberAuthService.logout(memberId, refreshToken);

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
    public ResponseEntity<Void> updateKakaoId(@CurrentMemberId long memberId,
        @RequestBody String kakaoId) {
        memberContactService.updateKakaoId(memberId, kakaoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "주요 연락처 수단 변경(Phone) API")
    @PatchMapping("/profile/contact/phone")
    public ResponseEntity<Void> updatePhoneNumber(@CurrentMemberId long memberId,
        @RequestBody String phone) {
        memberContactService.updatePhoneNumber(memberId, phone);
        return ResponseEntity.noContent().build();
    }
}
