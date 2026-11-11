package deepple.deepple.community.command.infra.profileexchange;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.community.command.domain.CommunityErrorCode;
import deepple.deepple.community.command.domain.profileexchange.ProfileExchange;
import deepple.deepple.community.command.domain.profileexchange.ProfileExchangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProfileExchangeRepositoryImpl implements ProfileExchangeRepository {
    private final ProfileExchangeJpaRepository profileExchangeJpaRepository;

    @Override
    public ProfileExchange save(final ProfileExchange profileExchange) {
        try {
            return profileExchangeJpaRepository.save(profileExchange);
        } catch (DataIntegrityViolationException e) {
            throw new DeeppleException(
                CommunityErrorCode.PROFILE_EXCHANGE_ALREADY_EXISTS,
                "requesterId=" + profileExchange.getRequesterId() + ", responderId=" + profileExchange.getResponderId(),
                e
            );
        }
    }

    @Override
    public boolean existsProfileExchangeBetween(final Long memberId, final Long anotherMemberId) {
        return profileExchangeJpaRepository.existsProfileExchangeBetween(memberId, anotherMemberId);
    }

    @Override
    public Optional<ProfileExchange> findById(final Long id) {
        return profileExchangeJpaRepository.findById(id);
    }
}