package deepple.deepple.notification.command.application;

import deepple.deepple.common.support.IntegrationTestSupport;
import deepple.deepple.notification.command.domain.NotificationTemplate;
import deepple.deepple.notification.command.domain.NotificationTemplateCommandRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static deepple.deepple.notification.command.domain.NotificationType.HEART_GRANTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class NotificationDataReaderIntegrationTest extends IntegrationTestSupport {

    private static final String CACHE_NAME = "notificationTemplate";

    @Autowired
    private NotificationDataReader notificationDataReader;

    @Autowired
    private NotificationTemplateCommandRepository notificationTemplateRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private NotificationTemplate original;

    @BeforeEach
    void setUp() {
        original = notificationTemplateRepository.findByType(HEART_GRANTED).orElseThrow();
        notificationTemplateRepository.delete(original);
        cacheManager.getCache(CACHE_NAME).evict(HEART_GRANTED);
    }

    @AfterEach
    void tearDown() {
        notificationTemplateRepository.findByType(HEART_GRANTED)
            .ifPresent(notificationTemplateRepository::delete);
        notificationTemplateRepository.save(
            NotificationTemplate.of(HEART_GRANTED, original.getTitleTemplate(), original.getBodyTemplate())
        );
        cacheManager.getCache(CACHE_NAME).evict(HEART_GRANTED);
    }

    @Test
    @DisplayName("findTemplate(): 템플릿이 없는 결과는 캐시하지 않음")
    void doesNotCacheMissingTemplate() {
        // given
        assertThat(notificationDataReader.findTemplate(HEART_GRANTED)).isEmpty();
        notificationTemplateRepository.save(NotificationTemplate.of(HEART_GRANTED, "제목", "본문"));

        // when
        var result = notificationDataReader.findTemplate(HEART_GRANTED);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().titleTemplate()).isEqualTo("제목");
    }

    @Test
    @DisplayName("findTemplate(): 템플릿 캐시는 1시간 뒤 만료")
    void cachesTemplateForOneHour() {
        // given
        notificationTemplateRepository.save(NotificationTemplate.of(HEART_GRANTED, "제목", "본문"));

        // when
        notificationDataReader.findTemplate(HEART_GRANTED);

        // then
        await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> assertThat(
                redisTemplate.getExpire("cache::" + CACHE_NAME + "::" + HEART_GRANTED, TimeUnit.SECONDS)
            ).isBetween(3500L, 3600L));
    }
}
