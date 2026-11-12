package kr.co.deepple.community.command.domain.profileexchange;

import java.util.Optional;

public interface ProfileExchangeRepository {
    ProfileExchange save(ProfileExchange profileExchange);

    boolean existsProfileExchangeBetween(Long memberId, Long anotherMemberId);
    
    Optional<ProfileExchange> findById(Long id);
}
