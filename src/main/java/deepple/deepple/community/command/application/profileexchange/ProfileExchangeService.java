package deepple.deepple.community.command.application.profileexchange;

import deepple.deepple.block.application.required.BlockRepository;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.common.repository.LockRepository;
import deepple.deepple.community.command.domain.CommunityErrorCode;
import deepple.deepple.community.command.domain.profileexchange.ProfileExchange;
import deepple.deepple.community.command.domain.profileexchange.ProfileExchangeRepository;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileExchangeService {
    private static final String LOCK_PREFIX = "ProfileExchange:";
    private final ProfileExchangeRepository profileExchangeRepository;
    private final LockRepository lockRepository;
    private final MemberCommandRepository memberCommandRepository;
    private final BlockRepository blockRepository;

    @Transactional
    public void request(Long requesterId, Long responderId) {
        String key = generateKey(requesterId, responderId);
        lockRepository.withNamedLock(key, () -> {
            validateProfileExchangeRequest(requesterId, responderId);
            String senderName = getNickNameByMemberId(requesterId);
            ProfileExchange profileExchange = ProfileExchange.request(requesterId, responderId, senderName);
            profileExchangeRepository.save(profileExchange);
        });
    }

    @Transactional
    public void approve(Long profileExchangeId, Long responderId) {
        String senderName = getNickNameByMemberId(responderId);
        ProfileExchange profileExchange = getProfileExchangeById(profileExchangeId);
        validateProfileExchangeResponse(profileExchange, responderId);
        profileExchange.approve(senderName);
    }

    @Transactional
    public void reject(Long profileExchangeId, Long responderId) {
        String senderName = getNickNameByMemberId(responderId);
        ProfileExchange profileExchange = getProfileExchangeById(profileExchangeId);
        validateProfileExchangeResponse(profileExchange, responderId);
        profileExchange.reject(senderName);
    }

    private String getNickNameByMemberId(Long memberId) {
        return memberCommandRepository.findById(memberId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId))
            .getProfile()
            .getNickname()
            .getValue();
    }

    private ProfileExchange getProfileExchangeById(Long profileExchangeId) {
        return profileExchangeRepository.findById(profileExchangeId)
            .orElseThrow(() -> new DeeppleException(
                CommunityErrorCode.PROFILE_EXCHANGE_NOT_FOUND,
                "profileExchangeId=" + profileExchangeId
            ));
    }

    private void validateProfileExchangeResponse(ProfileExchange profileExchange, Long responderId) {
        if (profileExchange.getResponderId() != responderId) {
            throw new DeeppleException(
                CommunityErrorCode.PROFILE_EXCHANGE_ACCESS_DENIED,
                "profileExchangeId=" + profileExchange.getId() + ", responderId=" + responderId
            );
        }
    }

    private String generateKey(Long requesterId, Long responderId) {
        return LOCK_PREFIX + Math.max(requesterId, responderId) + ":" + Math.min(requesterId, responderId);
    }

    private void validateProfileExchangeRequest(Long requesterId, Long responderId) {
        if (profileExchangeRepository.existsProfileExchangeBetween(requesterId, responderId)) {
            throw new DeeppleException(
                CommunityErrorCode.PROFILE_EXCHANGE_ALREADY_EXISTS,
                "requesterId=" + requesterId + ", responderId=" + responderId
            );
        }
        Member responder = memberCommandRepository.findById(responderId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + responderId));
        if (!responder.isActive()) {
            throw new DeeppleException(
                CommunityErrorCode.PROFILE_EXCHANGE_RESPONDER_INACTIVE,
                "responderId=" + responderId
            );
        }
        if (blockRepository.existsByBlockerIdAndBlockedId(requesterId, responderId)) {
            throw new DeeppleException(
                CommunityErrorCode.PROFILE_EXCHANGE_BLOCKED,
                "requesterId=" + requesterId + ", responderId=" + responderId
            );
        }
        if (blockRepository.existsByBlockerIdAndBlockedId(responderId, requesterId)) {
            throw new DeeppleException(
                CommunityErrorCode.PROFILE_EXCHANGE_BLOCKED,
                "requesterId=" + requesterId + ", responderId=" + responderId
            );
        }
    }
}
