package kr.co.deepple.common.lock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistributedLockTemplateTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private RLock lock;

    @InjectMocks
    private DistributedLockTemplate distributedLockTemplate;

    @Test
    @DisplayName("작업이 끝난 뒤 락 해제에 실패해도 작업 결과를 바꾸지 않는다")
    void keepActionResultWhenUnlockFails() throws InterruptedException {
        // given
        AtomicBoolean actionRan = new AtomicBoolean();
        givenLockAcquired();
        when(lock.isHeldByCurrentThread()).thenThrow(new RedisConnectionException("연결 끊김"));

        // when
        distributedLockTemplate.execute("key", () -> actionRan.set(true));

        // then
        assertThat(actionRan).isTrue();
    }

    @Test
    @DisplayName("작업이 실패하고 락 해제도 실패하면 작업 예외를 그대로 던진다")
    void keepActionExceptionWhenUnlockFails() throws InterruptedException {
        // given
        IllegalStateException actionException = new IllegalStateException("작업 실패");
        givenLockAcquired();
        when(lock.isHeldByCurrentThread()).thenThrow(new RedisConnectionException("연결 끊김"));

        // when & then
        assertThatThrownBy(() -> distributedLockTemplate.execute("key", () -> {
            throw actionException;
        }))
            .isSameAs(actionException);
    }

    private void givenLockAcquired() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);
    }
}
