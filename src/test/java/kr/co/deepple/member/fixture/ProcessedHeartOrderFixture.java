package kr.co.deepple.member.fixture;

import kr.co.deepple.member.command.domain.heartorder.ProcessedHeartOrder;
import kr.co.deepple.member.command.domain.heartorder.ProcessedHeartOperation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessedHeartOrderFixture {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private Long id;
    private ProcessedHeartOperation operationType = ProcessedHeartOperation.PURCHASE;
    private Long orderId;

    public static ProcessedHeartOrderFixture builder() {
        return new ProcessedHeartOrderFixture();
    }

    public ProcessedHeartOrder build() {
        long resolvedOrderId = orderId == null ? SEQUENCE.incrementAndGet() : orderId;
        ProcessedHeartOrder processedHeartOrder = ProcessedHeartOrder.of(operationType, resolvedOrderId);
        setField(processedHeartOrder, "id", id);
        return processedHeartOrder;
    }
}
