package kr.co.deepple.match.command.application.match;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.lock.DistributedLockTemplate;
import kr.co.deepple.heart.application.port.in.UseHeartCommand;
import kr.co.deepple.heart.application.port.in.UseHeartUseCase;
import kr.co.deepple.match.command.domain.match.Match;
import kr.co.deepple.match.command.domain.match.MatchContactType;
import kr.co.deepple.match.command.domain.match.MatchErrorCode;
import kr.co.deepple.match.command.domain.match.MatchRepository;
import kr.co.deepple.match.command.domain.match.MatchStatus;
import kr.co.deepple.match.command.domain.match.MatchType;
import kr.co.deepple.match.command.domain.match.vo.Message;
import kr.co.deepple.match.presentation.dto.MatchRequestDto;
import kr.co.deepple.match.presentation.dto.MatchResponseDto;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.introduction.IntroductionType;
import kr.co.deepple.member.command.domain.introduction.MemberIntroduction;
import kr.co.deepple.member.command.domain.introduction.MemberIntroductionCommandRepository;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchService {

  private static final String LOCK_PREFIX = "MATCH:";
  private static final String REQUEST_HEART_USAGE_TYPE = "MESSAGE";

  private final MemberCommandRepository memberCommandRepository;
  private final MemberIntroductionCommandRepository introductionCommandRepository;
  private final MatchRepository matchRepository;
  private final DistributedLockTemplate distributedLockTemplate;
  private final UseHeartUseCase useHeartUseCase;

  public void request(long requesterId, MatchRequestDto request) {
    long responderId = request.responderId();
    Message message = Message.from(request.requestMessage());
    MatchContactType contactType = MatchContactType.from(request.contactType());

    String key = generateKey(requesterId, responderId);
    distributedLockTemplate.executeInTransaction(
        key, () -> createMatch(requesterId, responderId, message, contactType));
  }

  private void createMatch(
      long requesterId, long responderId, Message message, MatchContactType contactType) {
    if (existsMutualMatch(requesterId, responderId)) {
      throw new DeeppleException(
          MatchErrorCode.MATCH_ALREADY_EXISTS,
          "requesterId=" + requesterId + ", responderId=" + responderId);
    }

    String requesterName = findNickname(requesterId);
    MatchType matchType = getMatchType(requesterId, responderId);
    Match match =
        Match.request(requesterId, responderId, message, requesterName, matchType, contactType);
    matchRepository.save(match);
    useHeartUseCase.useHeart(
        new UseHeartCommand(
            requesterId,
            REQUEST_HEART_USAGE_TYPE,
            matchType.name(),
            null,
            String.valueOf(match.getId())));
  }

  private MatchType getMatchType(long requesterId, long responderId) {
    Optional<MemberIntroduction> optionalIntroduction =
        introductionCommandRepository.findByMemberIdAndIntroducedMemberId(requesterId, responderId);
    if (optionalIntroduction.isEmpty()) {
      return MatchType.MATCH;
    }

    MemberIntroduction introduction = optionalIntroduction.get();
    if (introduction.getType() == IntroductionType.SOULMATE) {
      return MatchType.SOULMATE;
    }
    return MatchType.MATCH;
  }

  @Transactional
  public void approve(long matchId, long responderId, MatchResponseDto respondDto) {
    Match match = getWaitingMatchByIdAndResponderId(matchId, responderId);
    validateMatch(match);
    String responderName = findNickname(responderId);
    MatchContactType contactType = MatchContactType.from(respondDto.contactType());
    match.approve(Message.from(respondDto.responseMessage()), responderName, contactType);
  }

  @Transactional
  public void reject(long matchId, long responderId) {
    Match match = getWaitingMatchByIdAndResponderId(matchId, responderId);
    validateMatch(match);
    String responderName = findNickname(responderId);
    match.reject(responderName);
  }

  @Transactional
  public void rejectCheck(long requesterId, long matchId) {
    Match match = getRejectedMatchByIdAndRequesterId(matchId, requesterId);
    validateMatch(match);
    match.checkRejected();
  }

  @Transactional
  public void readReceivedMatch(long matchId, long readerId) {
    Match match =
        matchRepository
            .findByIdAndResponderId(matchId, readerId)
            .orElseThrow(
                () -> new DeeppleException(MatchErrorCode.MATCH_NOT_FOUND, "matchId=" + matchId));
    match.read(readerId);
  }

  @Transactional
  public void read(long readerId, long matchRequesterId, long matchResponderId) {
    validateReader(readerId, matchRequesterId, matchResponderId);
    Match match =
        matchRepository
            .findByRequesterIdAndResponderId(matchRequesterId, matchResponderId)
            .orElseThrow(() ->
                new DeeppleException(
                    MatchErrorCode.MATCH_NOT_FOUND,
                    "requesterId=" + matchRequesterId + ", responderId=" + matchResponderId));
    match.read(readerId);
  }

  private void validateReader(long readerId, long matchRequesterId, long matchResponderId) {
    if (readerId != matchRequesterId && readerId != matchResponderId) {
      throw new DeeppleException(
          MatchErrorCode.MATCH_ACCESS_DENIED,
          "readerId="
              + readerId
              + ", requesterId="
              + matchRequesterId
              + ", responderId="
              + matchResponderId);
    }
  }

  private String findNickname(long memberId) {
    return memberCommandRepository
        .findById(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId))
        .getProfile()
        .getNickname()
        .getValue();
  }

  private boolean existsMutualMatch(long requesterId, long responderId) {
    return matchRepository.existsActiveMatchBetween(requesterId, responderId);
  }

  private String generateKey(long requesterId, long responderId) {
    return LOCK_PREFIX
        + Math.max(requesterId, responderId)
        + ":"
        + Math.min(requesterId, responderId);
  }

  private Match getWaitingMatchByIdAndResponderId(long id, long responderId) {
    Match match =
        matchRepository
            .findByIdAndResponderId(id, responderId)
            .orElseThrow(
                () -> new DeeppleException(MatchErrorCode.MATCH_NOT_FOUND, "matchId=" + id));

    if (match.getStatus() != MatchStatus.WAITING) {
      throw new DeeppleException(
          MatchErrorCode.INVALID_MATCH_STATUS_CHANGE,
          "matchId=" + id + ", status=" + match.getStatus());
    }
    return match;
  }

  private Match getRejectedMatchByIdAndRequesterId(long id, long requesterId) {
    Match match =
        matchRepository
            .findByIdAndRequesterId(id, requesterId)
            .orElseThrow(
                () -> new DeeppleException(MatchErrorCode.MATCH_NOT_FOUND, "matchId=" + id));

    if (match.getStatus() != MatchStatus.REJECTED) {
      throw new DeeppleException(
          MatchErrorCode.INVALID_MATCH_STATUS_CHANGE,
          "matchId=" + id + ", status=" + match.getStatus());
    }
    return match;
  }

  private void validateMatch(Match match) {
    Member member =
        memberCommandRepository
            .findById(match.getRequesterId())
            .orElseThrow(() ->
                new DeeppleException(
                    MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + match.getRequesterId()));
    if (!member.isActive()) {
      throw new DeeppleException(
          MatchErrorCode.MATCH_REQUESTER_INACTIVE, "requesterId=" + member.getId());
    }
  }
}
