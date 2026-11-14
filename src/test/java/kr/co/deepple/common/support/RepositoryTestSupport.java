package kr.co.deepple.common.support;

import kr.co.deepple.auth.application.port.out.AuthMessageRepository;
import kr.co.deepple.auth.application.port.out.RefreshTokenRepository;
import kr.co.deepple.common.config.MySqlContainerConfig;
import kr.co.deepple.common.config.QueryDslConfig;
import kr.co.deepple.recommendation.application.port.out.IntroductionCacheRepository;
import kr.co.deepple.recommendation.application.port.out.PersonalityIntroductionOpenStateRepository;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Repository;

@DataJpaTest(
    includeFilters = @Filter(Repository.class),
    excludeFilters =
        @Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {
              RefreshTokenRepository.class,
              AuthMessageRepository.class,
              IntroductionCacheRepository.class,
              PersonalityIntroductionOpenStateRepository.class
            }))
@Import({QueryDslConfig.class, MySqlContainerConfig.class})
public abstract class RepositoryTestSupport {}
