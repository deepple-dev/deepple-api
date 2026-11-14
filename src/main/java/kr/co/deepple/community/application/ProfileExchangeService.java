package kr.co.deepple.community.application;

import kr.co.deepple.block.application.port.out.BlockRepository;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.lock.DistributedLockTemplate;
import kr.co.deepple.community.application.port.out.ProfileExchangeRepository;
import kr.co.deepple.community.domain.CommunityErrorCode;
import kr.co.deepple.community.domain.ProfileExchange;
import kr.co.deepple.community.event.ProfileExchangeAcceptedEvent;
import kr.co.deepple.community.event.ProfileExchangeRejectedEvent;
import kr.co.deepple.community.event.ProfileExchangeRequestedEvent;
import kr.co.deepple.heart.application.port.in.UseHeartCommand;
import kr.co.deepple.heart.application.port.in.UseHeartUseCase;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.application.port.in.MemberSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileExchangeService {

  private static final String LOCK_PREFIX = "PROFILE_EXCHANGE:";
  private static final String HEART_USAGE_TYPE = "PROFILE_EXCHANGE";

  private final ProfileExchangeRepository profileExchangeRepository;
  private final DistributedLockTemplate distributedLockTemplate;
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final BlockRepository blockRepository;
  private final UseHeartUseCase useHeartUseCase;
  private final ApplicationEventPublisher eventPublisher;

  public void request(long requesterId, long responderId) {
    String key = generateKey(requesterId, responderId);
    distributedLockTemplate.executeInTransaction(
        key, () -> createProfileExchange(requesterId, responderId));
  }

  private void createProfileExchange(long requesterId, long responderId) {
    validateProfileExchangeRequest(requesterId, responderId);
    String senderName = getNickNameByMemberId(requesterId);
    ProfileExchange profileExchange = ProfileExchange.request(requesterId, responderId);
    profileExchangeRepository.save(profileExchange);
    useHeartUseCase.useHeart(
        new UseHeartCommand(
            requesterId,
            HEART_USAGE_TYPE,
            HEART_USAGE_TYPE,
            null,
            String.valueOf(profileExchange.getId())));
    eventPublisher.publishEvent(
        ProfileExchangeRequestedEvent.of(requesterId, responderId, senderName));
  }

  @Transactional
  public void approve(long profileExchangeId, long responderId) {
    String senderName = getNickNameByMemberId(responderId);
    ProfileExchange profileExchange = getProfileExchangeById(profileExchangeId);
    validateProfileExchangeResponse(profileExchange, responderId);
    profileExchange.approve();
    eventPublisher.publishEvent(
        ProfileExchangeAcceptedEvent.of(
            profileExchange.getRequesterId(), profileExchange.getResponderId(), senderName));
  }

  @Transactional
  public void reject(long profileExchangeId, long responderId) {
    String senderName = getNickNameByMemberId(responderId);
    ProfileExchange profileExchange = getProfileExchangeById(profileExchangeId);
    validateProfileExchangeResponse(profileExchange, responderId);
    profileExchange.reject();
    eventPublisher.publishEvent(
        ProfileExchangeRejectedEvent.of(
            profileExchange.getRequesterId(), profileExchange.getResponderId(), senderName));
  }

  private String getNickNameByMemberId(long memberId) {
    return findMemberSummaryUseCase.getMemberSummary(memberId).requireNickname();
  }

  private ProfileExchange getProfileExchangeById(long profileExchangeId) {
    return profileExchangeRepository
        .findById(profileExchangeId)
        .orElseThrow(() ->
            new DeeppleException(
                CommunityErrorCode.PROFILE_EXCHANGE_NOT_FOUND,
                "profileExchangeId=" + profileExchangeId));
  }

  private void validateProfileExchangeResponse(ProfileExchange profileExchange, long responderId) {
    if (profileExchange.getResponderId() != responderId) {
      throw new DeeppleException(
          CommunityErrorCode.PROFILE_EXCHANGE_ACCESS_DENIED,
          "profileExchangeId=" + profileExchange.getId() + ", responderId=" + responderId);
    }
  }

  private String generateKey(long requesterId, long responderId) {
    return LOCK_PREFIX
        + Math.max(requesterId, responderId)
        + ":"
        + Math.min(requesterId, responderId);
  }

  private void validateProfileExchangeRequest(long requesterId, long responderId) {
    if (profileExchangeRepository.existsProfileExchangeBetween(requesterId, responderId)) {
      throw new DeeppleException(
          CommunityErrorCode.PROFILE_EXCHANGE_ALREADY_EXISTS,
          "requesterId=" + requesterId + ", responderId=" + responderId);
    }
    MemberSummary responder = findMemberSummaryUseCase.getMemberSummary(responderId);
    if (!responder.isActive()) {
      throw new DeeppleException(
          CommunityErrorCode.PROFILE_EXCHANGE_RESPONDER_INACTIVE, "responderId=" + responderId);
    }
    if (blockRepository.existsByBlockerIdAndBlockedId(requesterId, responderId)) {
      throw new DeeppleException(
          CommunityErrorCode.PROFILE_EXCHANGE_BLOCKED,
          "requesterId=" + requesterId + ", responderId=" + responderId);
    }
    if (blockRepository.existsByBlockerIdAndBlockedId(responderId, requesterId)) {
      throw new DeeppleException(
          CommunityErrorCode.PROFILE_EXCHANGE_BLOCKED,
          "requesterId=" + requesterId + ", responderId=" + responderId);
    }
  }
}
