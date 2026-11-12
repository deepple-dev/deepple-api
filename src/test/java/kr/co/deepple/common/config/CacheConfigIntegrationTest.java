package kr.co.deepple.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.datingexam.application.dto.DatingExamAnswerInfo;
import kr.co.deepple.datingexam.application.dto.DatingExamInfoResponse;
import kr.co.deepple.datingexam.application.dto.DatingExamQuestionInfo;
import kr.co.deepple.datingexam.application.dto.DatingExamSubjectInfo;
import kr.co.deepple.notification.command.application.NotificationTemplateInfo;
import kr.co.deepple.notification.command.domain.NotificationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;

@DisplayName("Redis 캐시 설정 통합 테스트")
class CacheConfigIntegrationTest extends IntegrationTestSupport {

  private static final String KEY_PREFIX = "cache:v2::";
  private static final String KEY = "cache-config-test";

  @Autowired private CacheManager cacheManager;

  @Autowired private StringRedisTemplate redisTemplate;

  @AfterEach
  void tearDown() {
    redisTemplate.delete(
        List.of(
            KEY_PREFIX + "datingExamInfo::" + KEY,
            KEY_PREFIX + "notificationTemplate::" + KEY,
            KEY_PREFIX + "undeclared::" + KEY));
  }

  @Test
  @DisplayName("연애고사 정보 캐시는 값을 그대로 복원하고 1시간 뒤 만료된다")
  void restoreDatingExamInfo() {
    // given
    DatingExamAnswerInfo answer = new DatingExamAnswerInfo(1L, "답");
    DatingExamQuestionInfo question = new DatingExamQuestionInfo(1L, "질문", List.of(answer));
    DatingExamSubjectInfo subject =
        new DatingExamSubjectInfo(1L, "REQUIRED", "가치관", List.of(question));
    DatingExamInfoResponse value = new DatingExamInfoResponse(List.of(subject));
    Cache cache = cacheManager.getCache("datingExamInfo");

    // when
    cache.put(KEY, value);

    // then
    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> assertThat(cache.get(KEY, DatingExamInfoResponse.class)).isEqualTo(value));
    assertThat(redisTemplate.opsForValue().get(KEY_PREFIX + "datingExamInfo::" + KEY))
        .doesNotContain("@class");
    assertThat(redisTemplate.getExpire(KEY_PREFIX + "datingExamInfo::" + KEY, TimeUnit.SECONDS))
        .isBetween(3500L, 3600L);
  }

  @Test
  @DisplayName("알림 템플릿 캐시는 값을 그대로 복원하고 1시간 뒤 만료된다")
  void restoreNotificationTemplate() {
    // given
    NotificationTemplateInfo value =
        new NotificationTemplateInfo(1L, NotificationType.values()[0], "제목", "본문");
    Cache cache = cacheManager.getCache("notificationTemplate");

    // when
    cache.put(KEY, value);

    // then
    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> assertThat(cache.get(KEY, NotificationTemplateInfo.class)).isEqualTo(value));
    assertThat(redisTemplate.opsForValue().get(KEY_PREFIX + "notificationTemplate::" + KEY))
        .doesNotContain("@class");
    assertThat(
            redisTemplate.getExpire(KEY_PREFIX + "notificationTemplate::" + KEY, TimeUnit.SECONDS))
        .isBetween(3500L, 3600L);
  }

  @Test
  @DisplayName("따로 설정하지 않은 캐시도 기본 1시간 뒤 만료된다")
  void expireUndeclaredCacheByDefault() {
    // given
    Cache cache = cacheManager.getCache("undeclared");

    // when
    cache.put(KEY, "value");

    // then
    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(() ->
            assertThat(redisTemplate.getExpire(KEY_PREFIX + "undeclared::" + KEY, TimeUnit.SECONDS))
                .isBetween(3500L, 3600L));
  }
}
