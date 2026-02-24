package deepple.deepple.member.command.application.member;

import deepple.deepple.admin.command.domain.suspension.Suspension;
import deepple.deepple.admin.command.domain.suspension.SuspensionCommandRepository;
import deepple.deepple.admin.command.domain.suspension.SuspensionStatus;
import deepple.deepple.auth.domain.TokenParser;
import deepple.deepple.auth.domain.TokenProvider;
import deepple.deepple.auth.domain.TokenRepository;
import deepple.deepple.common.enums.Role;
import deepple.deepple.common.event.Events;
import deepple.deepple.member.command.application.member.dto.MemberLoginServiceDto;
import deepple.deepple.member.command.application.member.dto.TokenPairResponse;
import deepple.deepple.member.command.application.member.exception.*;
import deepple.deepple.member.command.application.member.sms.AuthMessageService;
import deepple.deepple.member.command.domain.member.ActivityStatus;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.command.domain.member.event.MemberLoggedInEvent;
import deepple.deepple.member.command.domain.member.event.MemberLoggedOutEvent;
import deepple.deepple.member.command.domain.member.event.MemberRegisteredEvent;
import deepple.deepple.member.command.domain.member.exception.MemberNotActiveException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberAuthService {

    private final static List<ActivityStatus> ALLOWED_ACTIVITY_STATUS = List.of(ActivityStatus.ACTIVE,
        ActivityStatus.INITIAL, ActivityStatus.WAITING_SCREENING, ActivityStatus.REJECTED_SCREENING);
    private final MemberCommandRepository memberCommandRepository;
    private final SuspensionCommandRepository suspensionRepository;
    private final AuthMessageService authMessageService;
    private final TokenProvider tokenProvider;
    private final TokenParser tokenParser;
    private final TokenRepository tokenRepository;
    @Value("${auth.prefix-code}")
    private String PRE_FIXED_CODE;

    @Transactional
    public MemberLoginServiceDto login(String phoneNumber, String code) {
        if (PRE_FIXED_CODE == null || PRE_FIXED_CODE.isBlank() || !PRE_FIXED_CODE.equals(code)) {
            authMessageService.authenticate(phoneNumber, code);
        }

        Member member = createOrFindMemberByPhoneNumber(phoneNumber);
        validateMemberLoginPermission(member);

        String accessToken = tokenProvider.createAccessToken(member.getId(), Role.MEMBER, Instant.now());
        String refreshToken = tokenProvider.createRefreshToken(member.getId(), Role.MEMBER, Instant.now());
        tokenRepository.save(refreshToken);

        Events.raise(MemberLoggedInEvent.from(member.getId()));

        return new MemberLoginServiceDto(accessToken, refreshToken, member.isProfileSettingNeeded(),
            member.getActivityStatus() != null ? member.getActivityStatus().name() : null);
    }

    @Transactional
    public MemberLoginServiceDto test(String phoneNumber) {
        Member member = createOrFindMemberByPhoneNumber(phoneNumber);

        validateMemberLoginPermission(member);

        String accessToken = tokenProvider.createAccessToken(member.getId(), Role.MEMBER, Instant.now());
        String refreshToken = tokenProvider.createRefreshToken(member.getId(), Role.MEMBER, Instant.now());
        tokenRepository.save(refreshToken);

        return new MemberLoginServiceDto(accessToken, refreshToken, member.isProfileSettingNeeded(),
            member.getActivityStatus() != null ? member.getActivityStatus().name() : null);
    }

    public void logout(long memberId, String token) {
        Events.raise(MemberLoggedOutEvent.from(memberId));
        deleteToken(token);
    }

    public TokenPairResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new MissingRefreshTokenException();
        }

        if (tokenParser.isExpired(refreshToken)) {
            tokenRepository.delete(refreshToken);
            throw new ExpiredRefreshTokenException();
        }

        if (!tokenParser.isValid(refreshToken) || !tokenRepository.exists(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        tokenRepository.delete(refreshToken);

        long memberId = tokenParser.getId(refreshToken);
        Role role = tokenParser.getRole(refreshToken);
        Instant issuedAt = Instant.now();

        String newAccessToken = tokenProvider.createAccessToken(memberId, role, issuedAt);
        String newRefreshToken = tokenProvider.createRefreshToken(memberId, role, issuedAt);
        tokenRepository.save(newRefreshToken);

        return new TokenPairResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void delete(Long memberId, String token) {
        Member member = getMemberById(memberId);

        if (tokenParser.isValid(token) && tokenParser.getId(token) == member.getId()) {
            deleteToken(token);
        }
        member.delete();
    }

    public void sendAuthCode(String phoneNumber) {
        authMessageService.sendAndSaveCode(phoneNumber);
    }

    private void deleteToken(String token) {
        tokenRepository.delete(token);
    }

    private Member createOrFindMemberByPhoneNumber(String phoneNumber) {
        return memberCommandRepository.findByPhoneNumber(phoneNumber).orElseGet(() -> create(phoneNumber));
    }

    private Member getMemberById(Long memberId) {
        return memberCommandRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
    }

    private Member create(String phoneNumber) {
        try {
            Member member = memberCommandRepository.save(Member.fromPhoneNumber(phoneNumber));
            Events.raise(new MemberRegisteredEvent(member.getId())); // Event 발행.
            return member;
        } catch (DataIntegrityViolationException e) {
            throw new MemberLoginConflictException(phoneNumber);
        }
    }

    private void validateMemberLoginPermission(Member member) {
        if (member.isDeleted()) { // 회원이 삭제된 경우.
            throw new MemberDeletedException();
        }

        ActivityStatus activityStatus = member.getActivityStatus();

        if (activityStatus == ActivityStatus.SUSPENDED_PERMANENTLY) { // 영구 정지일 경우.
            throw new PermanentlySuspendedMemberException();
        } else if (activityStatus == ActivityStatus.SUSPENDED_TEMPORARILY) { // 일시 정지일 경우.
            LocalDateTime suspensionExpireAt = getTemporarySuspensionExpireAt(member.getId());
            throw new TemporarilySuspendedMemberException(suspensionExpireAt);
        } else if (!ALLOWED_ACTIVITY_STATUS.contains(activityStatus)) {
            /**
             * 2025/11/18 변경 : 이외의 상태 (활동중, 심사 대기중, 심사 거절) 는 로그인 허용.
             */
            throw new MemberNotActiveException();
        }
    }

    private LocalDateTime getTemporarySuspensionExpireAt(Long memberId) {
        Optional<Suspension> optionalSuspension = suspensionRepository.findByMemberIdAndStatusOrderByExpireAtDesc(
            memberId, SuspensionStatus.TEMPORARY);
        return optionalSuspension
            .map(Suspension::getExpireAt)
            .map(instant -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
            .orElse(null);
    }
}
