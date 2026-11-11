package kr.co.deepple.common.support;

import kr.co.deepple.auth.infra.RefreshTokenRepository;
import kr.co.deepple.common.MockEventsExtension;
import kr.co.deepple.common.config.MySqlContainerConfig;
import kr.co.deepple.common.config.QueryDslConfig;
import kr.co.deepple.member.command.infra.member.AuthMessageRedisRepository;
import kr.co.deepple.member.query.introduction.intra.IntroductionRedisRepository;
import kr.co.deepple.member.query.introduction.intra.PersonalityIntroductionRedisRepository;
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
            PersonalityIntroductionRedisRepository.class
        }
    )
)
@Import({QueryDslConfig.class, MySqlContainerConfig.class})
@ExtendWith(MockEventsExtension.class)
public abstract class RepositoryTestSupport {
}
