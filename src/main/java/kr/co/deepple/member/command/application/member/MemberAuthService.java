package kr.co.deepple.member.command.application.member;

import static kr.co.deepple.member.command.domain.member.ActivityStatus.ACTIVE;
import static kr.co.deepple.member.command.domain.member.ActivityStatus.DORMANT;
import static kr.co.deepple.member.command.domain.member.ActivityStatus.INITIAL;
import static kr.co.deepple.member.command.domain.member.ActivityStatus.REJECTED_SCREENING;
import static kr.co.deepple.member.command.domain.member.ActivityStatus.SUSPENDED_PERMANENTLY;
import static kr.co.deepple.member.command.domain.member.ActivityStatus.SUSPENDED_TEMPORARILY;
import static kr.co.deepple.member.command.domain.member.ActivityStatus.WAITING_SCREENING;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.admin.command.domain.suspension.Suspension;
import kr.co.deepple.admin.command.domain.suspension.SuspensionCommandRepository;
import kr.co.deepple.admin.command.domain.suspension.SuspensionStatus;
import kr.co.deepple.auth.application.RefreshTokenService;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.application.member.dto.MemberLoginServiceDto;
import kr.co.deepple.member.command.application.member.dto.TokenPairResponse;
import kr.co.deepple.member.command.application.member.sms.AuthMessageService;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.event.MemberLoggedInEvent;
import kr.co.deepple.member.event.MemberLoggedOutEvent;
import kr.co.deepple.member.event.MemberRegisteredEvent;
import kr.co.deepple.member.event.MemberSummaryChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAuthService {

  private static final String SUSPENSION_EXPIRE_AT_PROPERTY = "suspensionExpireAt";
  private static final List<ActivityStatus> ALLOWED_ACTIVITY_STATUS =
      List.of(ACTIVE, INITIAL, WAITING_SCREENING, REJECTED_SCREENING, DORMANT);
  private final MemberCommandRepository memberCommandRepository;
  private final SuspensionCommandRepository suspensionRepository;
  private final AuthMessageService authMessageService;
  private final TokenProvider tokenProvider;
  private final RefreshTokenService refreshTokenService;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public MemberLoginServiceDto login(String phoneNumber, String code) {
    authMessageService.authenticate(phoneNumber, code);

    Optional<Member> existingMember = memberCommandRepository.findByPhoneNumber(phoneNumber);
    Member member = existingMember.orElseGet(() -> create(phoneNumber));
    validateMemberLoginPermission(member);

    String accessToken =
        tokenProvider.createAccessToken(member.getId(), Role.MEMBER, Instant.now());
    String refreshToken = refreshTokenService.issue(TokenOwner.member(member.getId()));

    if (!member.isDormant()) {
      eventPublisher.publishEvent(MemberLoggedInEvent.from(member.getId()));
    }

    memberCommandRepository.updateLastAccessedAt(
        member.getId(), LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));

    log.atInfo()
        .addKeyValue("memberId", member.getId())
        .addKeyValue("newMember", existingMember.isEmpty())
        .log("회원 로그인");

    return new MemberLoginServiceDto(
        accessToken,
        refreshToken,
        member.isProfileSettingNeeded(),
        member.getActivityStatus() != null ? member.getActivityStatus().name() : null);
  }

  @Transactional
  public void logout(long memberId, String token) {
    eventPublisher.publishEvent(MemberLoggedOutEvent.from(memberId));
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

    memberCommandRepository.updateLastAccessedAt(
        owner.id(), LocalDateTime.ofInstant(issuedAt, ZoneId.systemDefault()));

    return new TokenPairResponse(newAccessToken, newRefreshToken);
  }

  @Transactional
  public void delete(long memberId) {
    Member member = getMemberById(memberId);
    refreshTokenService.revokeAll(TokenOwner.member(memberId));
    member.delete();
    log.info("회원 탈퇴");
  }

  public void sendAuthCode(String phoneNumber) {
    authMessageService.sendAndSaveCode(phoneNumber);
  }

  private Member getMemberById(long memberId) {
    return memberCommandRepository
        .findById(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
  }

  private Member create(String phoneNumber) {
    try {
      Member member = memberCommandRepository.save(Member.fromPhoneNumber(phoneNumber));
      eventPublisher.publishEvent(MemberRegisteredEvent.from(member.getId()));
      eventPublisher.publishEvent(
          MemberSummaryChangedEvent.of(member.getId(), null, member.isVip()));
      return member;
    } catch (DataIntegrityViolationException e) {
      throw new ConcurrencyFailureException("같은 전화번호로 회원 가입이 동시에 요청되었습니다.", e);
    }
  }

  private void validateRefreshPermission(TokenOwner owner) {
    Member member = memberCommandRepository.findById(owner.id()).orElse(null);
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
      throw new DeeppleException(MemberErrorCode.MEMBER_DELETED, "memberId=" + member.getId());
    }

    ActivityStatus activityStatus = member.getActivityStatus();

    if (activityStatus == SUSPENDED_PERMANENTLY) { // 영구 정지일 경우.
      throw new DeeppleException(
          MemberErrorCode.MEMBER_PERMANENTLY_SUSPENDED, "memberId=" + member.getId());
    } else if (activityStatus == SUSPENDED_TEMPORARILY) { // 일시 정지일 경우.
      LocalDateTime suspensionExpireAt = getTemporarySuspensionExpireAt(member.getId());
      throw new DeeppleException(
              MemberErrorCode.MEMBER_TEMPORARILY_SUSPENDED, "memberId=" + member.getId())
          .withProperty(SUSPENSION_EXPIRE_AT_PROPERTY, suspensionExpireAt);
    } else if (!ALLOWED_ACTIVITY_STATUS.contains(activityStatus)) {
      /** 2025/11/18 변경 : 이외의 상태 (활동중, 심사 대기중, 심사 거절) 는 로그인 허용. */
      throw new DeeppleException(
          MemberErrorCode.MEMBER_NOT_ACTIVE,
          "memberId=" + member.getId() + ", activityStatus=" + activityStatus);
    }
  }

  private LocalDateTime getTemporarySuspensionExpireAt(long memberId) {
    Optional<Suspension> optionalSuspension =
        suspensionRepository.findByMemberIdAndStatusOrderByExpireAtDesc(
            memberId, SuspensionStatus.TEMPORARY);
    return optionalSuspension
        .map(Suspension::getExpireAt)
        .map(instant -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
        .orElse(null);
  }
}
