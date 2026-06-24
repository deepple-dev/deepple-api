package deepple.deepple.match.command.application.match;

import deepple.deepple.common.repository.LockRepository;
import deepple.deepple.match.command.application.match.exception.ExistsMatchException;
import deepple.deepple.match.command.application.match.exception.InvalidMatchUpdateException;
import deepple.deepple.match.command.application.match.exception.MatchNotFoundException;
import deepple.deepple.match.command.domain.match.*;
import deepple.deepple.match.command.domain.match.vo.Message;
import deepple.deepple.match.presentation.dto.MatchRequestDto;
import deepple.deepple.match.presentation.dto.MatchResponseDto;
import deepple.deepple.member.command.domain.introduction.IntroductionType;
import deepple.deepple.member.command.domain.introduction.MemberIntroduction;
import deepple.deepple.member.command.domain.introduction.MemberIntroductionCommandRepository;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchService {

    private static final String LOCK_PREFIX = "MATCH:";

    private final MemberCommandRepository memberCommandRepository;
    private final MemberIntroductionCommandRepository introductionCommandRepository;
    private final MatchRepository matchRepository;
    private final LockRepository lockRepository;

    @Transactional
    public void request(Long requesterId, MatchRequestDto request) {
        long responderId = request.responderId();
        String requesterName = findNickname(requesterId);
        MatchType matchType = getMatchType(requesterId, responderId);
        MatchContactType contactType = MatchContactType.valueOf(request.contactType());

        String key = generateKey(requesterId, responderId);
        lockRepository.withNamedLock(key, () -> {
            if (existsMutualMatch(requesterId, responderId)) {
                throw new ExistsMatchException();
            }

            Match match = Match.request(
                requesterId,
                responderId,
                Message.from(request.requestMessage()),
                requesterName,
                matchType,
                contactType
            );
            matchRepository.save(match);
        });
    }

    private MatchType getMatchType(Long requesterId, Long responderId) {
        Optional<MemberIntroduction> optionalIntroduction = introductionCommandRepository.findByMemberIdAndIntroducedMemberId(
            requesterId, responderId);
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
    public void approve(Long matchId, Long responderId, MatchResponseDto respondDto) {
        Match match = getWaitingMatchByIdAndResponderId(matchId, responderId);
        validateMatch(match);
        String responderName = findNickname(responderId);
        MatchContactType contactType = MatchContactType.valueOf(respondDto.contactType());
        match.approve(Message.from(respondDto.responseMessage()), responderName, contactType);
    }

    @Transactional
    public void reject(Long matchId, Long responderId) {
        Match match = getWaitingMatchByIdAndResponderId(matchId, responderId);
        validateMatch(match);
        String responderName = findNickname(responderId);
        match.reject(responderName);
    }

    @Transactional
    public void rejectCheck(Long requesterId, Long matchId) {
        Match match = getRejectedMatchByIdAndRequesterId(matchId, requesterId);
        validateMatch(match);
        match.checkRejected();
    }

    @Transactional
    public void readReceivedMatch(Long matchId, Long readerId) {
        Match match = matchRepository.findByIdAndResponderId(matchId, readerId)
            .orElseThrow(MatchNotFoundException::new);
        match.read(readerId);
    }

    @Transactional
    public void read(Long readerId, Long matchRequesterId, Long matchResponderId) {
        validateReader(readerId, matchRequesterId, matchResponderId);
        Match match = matchRepository.findByRequesterIdAndResponderId(matchRequesterId, matchResponderId)
            .orElseThrow(MatchNotFoundException::new);
        match.read(readerId);
    }

    private void validateReader(Long readerId, Long matchRequesterId, Long matchResponderId) {
        if (!readerId.equals(matchRequesterId) && !readerId.equals(matchResponderId)) {
            throw new IllegalArgumentException("readerId가 매치 당사자가 아닙니다.");
        }
    }

    private String findNickname(long memberId) {
        return memberCommandRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("Member not found. id: " + memberId))
            .getProfile()
            .getNickname()
            .getValue();
    }

    private boolean existsMutualMatch(Long requesterId, Long responderId) {
        return matchRepository.existsActiveMatchBetween(requesterId, responderId);
    }

    private String generateKey(Long requesterId, Long responderId) {
        return LOCK_PREFIX + Math.max(requesterId, responderId) + ":" + Math.min(requesterId, responderId);
    }

    private Match getWaitingMatchByIdAndResponderId(Long id, Long responderId) {
        Match match = matchRepository.findByIdAndResponderId(id, responderId)
            .orElseThrow(MatchNotFoundException::new);

        if (match.getStatus() != MatchStatus.WAITING) {
            throw new InvalidMatchUpdateException();
        }
        return match;
    }

    private Match getRejectedMatchByIdAndRequesterId(Long id, Long requesterId) {
        Match match = matchRepository.findByIdAndRequesterId(id, requesterId)
            .orElseThrow(MatchNotFoundException::new);

        if (match.getStatus() != MatchStatus.REJECTED) {
            throw new InvalidMatchUpdateException();
        }
        return match;
    }

    private void validateMatch(Match match) {
        Member member = memberCommandRepository.findById(match.getRequesterId()).orElseThrow(
            () -> new EntityNotFoundException("매치 요청자를 찾을 수 없습니다." + match.getRequesterId()));
        if (!member.isActive()) {
            throw new IllegalStateException("매치 요청자가 활성 상태가 아닙니다.");
        }
    }
}
