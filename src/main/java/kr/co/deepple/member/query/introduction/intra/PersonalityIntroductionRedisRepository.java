package kr.co.deepple.member.query.introduction.intra;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class PersonalityIntroductionRedisRepository {
    private static final String OPEN_PREFIX = "introduction:personality:open:";
    private static final String UNLOCKED_PREFIX = "introduction:personality:unlocked:";
    private static final Duration TTL = Duration.ofHours(24);

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public void saveOpenState(long memberId, PersonalityIntroductionOpenState state) {
        redisTemplate.opsForValue().set(openKey(memberId), objectMapper.writeValueAsString(state), TTL);
    }

    public Optional<PersonalityIntroductionOpenState> findOpenState(long memberId) {
        String json = redisTemplate.opsForValue().get(openKey(memberId));
        if (json == null) {
            return Optional.empty();
        }
        return Optional.of(objectMapper.readValue(json, PersonalityIntroductionOpenState.class));
    }

    public Set<Long> findUnlockedMemberIds(long memberId) {
        String json = redisTemplate.opsForValue().get(unlockedKey(memberId));
        if (json == null) {
            return new HashSet<>();
        }
        return objectMapper.readValue(json, new TypeReference<Set<Long>>() {});
    }

    public void addUnlockedMemberId(long memberId, long targetMemberId) {
        Set<Long> unlockedMemberIds = findUnlockedMemberIds(memberId);
        unlockedMemberIds.add(targetMemberId);
        redisTemplate.opsForValue().set(unlockedKey(memberId), objectMapper.writeValueAsString(unlockedMemberIds));
        alignUnlockedExpiry(memberId);
    }

    /**
     * 언락 집합 키의 만료 시각을 오픈 상태 키와 동일한 창(window)으로 맞춘다.
     * 언락이 반복돼도 24시간 창이 연장되지 않도록 오픈 키의 남은 TTL을 따른다.
     */
    private void alignUnlockedExpiry(long memberId) {
        Long remainingSeconds = redisTemplate.getExpire(openKey(memberId), TimeUnit.SECONDS);
        if (remainingSeconds != null && remainingSeconds > 0) {
            redisTemplate.expire(unlockedKey(memberId), Duration.ofSeconds(remainingSeconds));
        } else {
            redisTemplate.expire(unlockedKey(memberId), TTL);
        }
    }

    private String openKey(long memberId) {
        return OPEN_PREFIX + memberId;
    }

    private String unlockedKey(long memberId) {
        return UNLOCKED_PREFIX + memberId;
    }
}
