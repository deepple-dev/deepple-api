package kr.co.deepple.payment.fixture;

import kr.co.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseAmount;
import kr.co.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOption;
import kr.co.deepple.payment.command.domain.heartpurchaseoption.Price;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HeartPurchaseOptionFixture {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private Long id;
    private long amount = 45L;
    private long price = 9900L;
    private String productId;
    private String name = "45하트";

    public static HeartPurchaseOptionFixture builder() {
        return new HeartPurchaseOptionFixture();
    }

    public HeartPurchaseOption build() {
        String resolvedProductId = productId == null ? "TEST_ITEM_HEART_" + SEQUENCE.incrementAndGet() : productId;
        HeartPurchaseOption option = HeartPurchaseOption.of(HeartPurchaseAmount.from(amount), Price.from(price),
            resolvedProductId, name);
        setField(option, "id", id);
        return option;
    }
}
