package deepple.deepple.heart.fixture;

import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionType;
import deepple.deepple.heart.command.domain.heartusagepolicy.HeartPriceAmount;
import deepple.deepple.heart.command.domain.heartusagepolicy.HeartUsagePolicy;
import deepple.deepple.member.command.domain.member.Gender;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HeartUsagePolicyFixture {

    private Long id;
    private TransactionType transactionType = TransactionType.MESSAGE;
    private Gender gender = Gender.MALE;
    private long price = 20L;

    public static HeartUsagePolicyFixture builder() {
        return new HeartUsagePolicyFixture();
    }

    public HeartUsagePolicy build() {
        HeartUsagePolicy policy = HeartUsagePolicy.of(transactionType, gender, HeartPriceAmount.from(price));
        setField(policy, "id", id);
        return policy;
    }
}
