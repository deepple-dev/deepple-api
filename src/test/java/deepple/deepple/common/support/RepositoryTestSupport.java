package deepple.deepple.common.support;

import deepple.deepple.auth.infra.RefreshTokenRepository;
import deepple.deepple.common.MockEventsExtension;
import deepple.deepple.common.config.MySqlContainerConfig;
import deepple.deepple.common.config.QueryDslConfig;
import deepple.deepple.common.repository.RedissonLockRepository;
import deepple.deepple.member.command.infra.member.AuthMessageRedisRepository;
import deepple.deepple.member.query.introduction.intra.IntroductionRedisRepository;
import deepple.deepple.member.query.introduction.intra.PersonalityIntroductionRedisRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Repository;

@DataJpaTest(
    includeFilters = @Filter(Repository.class),
    excludeFilters = @Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
            RefreshTokenRepository.class,
            AuthMessageRedisRepository.class,
            IntroductionRedisRepository.class,
            PersonalityIntroductionRedisRepository.class,
            RedissonLockRepository.class
        }
    )
)
@Import({QueryDslConfig.class, MySqlContainerConfig.class})
@ExtendWith(MockEventsExtension.class)
public abstract class RepositoryTestSupport {
}
