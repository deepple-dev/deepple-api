package deepple.deepple.member.command.application.member;

import deepple.deepple.admin.command.domain.suspension.Suspension;
import deepple.deepple.admin.command.domain.suspension.SuspensionCommandRepository;
import deepple.deepple.admin.command.domain.suspension.SuspensionStatus;
import deepple.deepple.auth.domain.AuthErrorCode;
import deepple.deepple.auth.domain.RefreshTokenService;
import deepple.deepple.auth.domain.TokenOwner;
import deepple.deepple.auth.domain.TokenProvider;
import deepple.deepple.common.enums.Role;
import deepple.deepple.common.event.Events;
import deepple.deepple.common.exception.DeeppleException;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static deepple.deepple.member.command.domain.member.ActivityStatus.*;

@Service
@RequiredArgsConstructor
public class MemberAuthService {

    private final static List<ActivityStatus> ALLOWED_ACTIVITY_STATUS = List.of(
        ACTIVE, INITIAL, WAITING_SCREENING, REJECTED_SCREENING, DORMANT);
    private final MemberCommandRepository memberCommandRepository;
    private final SuspensionCommandRepository suspensionRepository;
    private final AuthMessageService authMessageService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public MemberLoginServiceDto login(String phoneNumber, String code) {
        authMessageService.authenticate(phoneNumber, code);

        Member member = createOrFindMemberByPhoneNumber(phoneNumber);
        validateMemberLoginPermission(member);

        String accessToken = tokenProvider.createAccessToken(member.getId(), Role.MEMBER, Instant.now());
        String refreshToken = refreshTokenService.issue(TokenOwner.member(member.getId()));

        if (!member.isDormant()) {
            Events.raise(MemberLoggedInEvent.from(member.getId()));
        }

        memberCommandRepository.updateLastAccessedAt(member.getId(),
            LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));

        return new MemberLoginServiceDto(accessToken, refreshToken, member.isProfileSettingNeeded(),
            member.getActivityStatus() != null ? member.getActivityStatus().name() : null);
    }

    @Transactional
    public void logout(long memberId, String token) {
        Events.raise(MemberLoggedOutEvent.from(memberId));
        refreshTokenService.revoke(token);
    }

    public TokenPairResponse refresh(String refreshToken) {
        TokenOwner owner = refreshTokenService.consume(refreshToken);
        if (!owner.isMember()) {
            throw new DeeppleException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        validateRefreshPermission(owner);

        Instant issuedAt = Instant.now();
        String newAccessToken = tokenProvider.createAccessToken(owner.id(), Role.MEMBER, issuedAt);
        String newRefreshToken = refreshTokenService.issue(owner);

        memberCommandRepository.updateLastAccessedAt(owner.id(),
            LocalDateTime.ofInstant(issuedAt, ZoneId.systemDefault()));

        return new TokenPairResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void delete(long memberId) {
        Member member = getMemberById(memberId);
        refreshTokenService.revokeAll(TokenOwner.member(memberId));
        member.delete();
    }

    public void sendAuthCode(String phoneNumber) {
        authMessageService.sendAndSaveCode(phoneNumber);
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
            throw new MemberLoginConflictException();
        }
    }

    private void validateRefreshPermission(TokenOwner owner) {
        Member member = memberCommandRepository.findById(owner.id())
            .orElse(null);
        if (member == null) {
            refreshTokenService.revokeAll(owner);
            throw new DeeppleException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        try {
            validateMemberLoginPermission(member);
        } catch (RuntimeException e) {
            refreshTokenService.revokeAll(owner);
            throw e;
        }
    }

    private void validateMemberLoginPermission(Member member) {
        if (member.isDeleted()) { // 회원이 삭제된 경우.
            throw new MemberDeletedException();
        }

        ActivityStatus activityStatus = member.getActivityStatus();

        if (activityStatus == SUSPENDED_PERMANENTLY) { // 영구 정지일 경우.
            throw new PermanentlySuspendedMemberException();
        } else if (activityStatus == SUSPENDED_TEMPORARILY) { // 일시 정지일 경우.
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
