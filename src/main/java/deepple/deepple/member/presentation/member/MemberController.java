package deepple.deepple.member.presentation.member;

import deepple.deepple.auth.presentation.CurrentMemberId;
import deepple.deepple.auth.presentation.RefreshTokenCookies;
import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.member.command.application.member.MemberAuthService;
import deepple.deepple.member.command.application.member.MemberContactService;
import deepple.deepple.member.command.application.member.MemberProfileService;
import deepple.deepple.member.presentation.member.dto.MemberChangeToActiveRequest;
import deepple.deepple.member.presentation.member.dto.MemberMyProfileResponse;
import deepple.deepple.member.presentation.member.dto.MemberProfileResponse;
import deepple.deepple.member.presentation.member.dto.MemberProfileUpdateRequest;
import deepple.deepple.member.query.member.application.MemberQueryService;
import deepple.deepple.member.query.member.view.HeartBalanceView;
import deepple.deepple.member.query.member.view.MemberContactView;
import deepple.deepple.member.query.member.view.MemberInfoView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    public ResponseEntity<BaseResponse<Void>> updateProfile(@RequestBody MemberProfileUpdateRequest request,
        @CurrentMemberId long memberId) {
        memberProfileService.updateMember(memberId, request);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "회원(자기 자신) 정보 캐싱용 API")
    @GetMapping("/cache")
    public ResponseEntity<BaseResponse<MemberInfoView>> getMyInfoCache(@CurrentMemberId long memberId) {
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK, memberQueryService.getInfoCache(memberId)));
    }

    @Operation(summary = "회원(자기 자신) 프로필 정보 조회 API")
    @GetMapping("/profile")
    public ResponseEntity<BaseResponse<MemberMyProfileResponse>> getMyProfile(@CurrentMemberId long memberId) {
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK,
            MemberMapper.toMemberMyProfileResponse(memberQueryService.getProfile(memberId))));
    }

    @Operation(summary = "상대방 프로필 조회 API")
    @GetMapping("/{memberId}")
    public ResponseEntity<BaseResponse<MemberProfileResponse>> getOtherProfile(@CurrentMemberId long memberId,
        @PathVariable("memberId") long otherMemberId) {
        return ResponseEntity.ok(
            BaseResponse.of(StatusType.OK, memberQueryService.getMemberProfile(memberId, otherMemberId)));
    }

    @Operation(summary = "휴면 계정 전환 API")
    @PostMapping("/profile/dormant")
    public ResponseEntity<BaseResponse<Void>> changeToDormant(
        @CurrentMemberId long memberId,
        @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        memberProfileService.changeToDormant(memberId);
        memberAuthService.logout(memberId, refreshToken);

        HttpHeaders headers = new HttpHeaders();
        ResponseCookie deleteCookie = refreshTokenCookies.expire();
        headers.add(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        return ResponseEntity.ok().headers(headers).body(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "활동 계정 전환 API")
    @PostMapping("/profile/active")
    public ResponseEntity<BaseResponse<Void>> changeToActive(@Valid @RequestBody MemberChangeToActiveRequest request) {
        memberProfileService.changeToActive(request.phoneNumber());
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "연락처(자기 자신) 정보 조회 API")
    @GetMapping("/profile/contact")
    public ResponseEntity<BaseResponse<MemberContactView>> getMyContact(@CurrentMemberId long memberId) {
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK, memberQueryService.getContact(memberId)));
    }

    @Operation(summary = "주요 연락처 수단(Kakao) 변경 API")
    @PatchMapping("/profile/contact/kakao")
    public ResponseEntity<BaseResponse<Void>> updateKakaoId(@CurrentMemberId long memberId,
        @RequestBody String kakaoId) {
        memberContactService.updateKakaoId(memberId, kakaoId);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "주요 연락처 수단 변경(Phone) API")
    @PatchMapping("/profile/contact/phone")
    public ResponseEntity<BaseResponse<Void>> updatePhoneNumber(@CurrentMemberId long memberId,
        @RequestBody String phone) {
        memberContactService.updatePhoneNumber(memberId, phone);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "보유 하트 조회 API")
    @GetMapping("/heartbalance")
    public ResponseEntity<BaseResponse<HeartBalanceView>> getHeartBalance(@CurrentMemberId long memberId) {
        return ResponseEntity.ok(
            BaseResponse.of(StatusType.OK, memberQueryService.getHeartBalance(memberId)));
    }
}
