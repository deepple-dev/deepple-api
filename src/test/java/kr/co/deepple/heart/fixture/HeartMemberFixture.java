package kr.co.deepple.heart.fixture;

import kr.co.deepple.heart.domain.Gender;
import kr.co.deepple.heart.domain.HeartMember;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HeartMemberFixture {
    private Long id;
    private long memberId = 1L;
    private Gender gender = Gender.MALE;
    private boolean isVip;
    private Instant sourceUpdatedAt = Instant.parse("2026-01-01T00:00:00Z");

    public static HeartMemberFixture builder() {
        return new HeartMemberFixture();
    }

    public HeartMember build() {
        HeartMember heartMember = HeartMember.of(memberId, gender, isVip, sourceUpdatedAt);
        setField(heartMember, "id", id);
        return heartMember;
    }
}
