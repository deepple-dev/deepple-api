package deepple.deepple.member.command.infra.member;

import deepple.deepple.common.MockEventsExtension;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(MemberCommandRepositoryImpl.class)
@ExtendWith(MockEventsExtension.class)
class MemberCommandRepositoryLastAccessedAtTest {

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("updateLastAccessedAt 호출 시 last_accessed_at만 갱신되고 updatedAt은 변하지 않는다")
    void updatesLastAccessedAtWithoutTouchingUpdatedAt() {
        // Given
        Member member = Member.fromPhoneNumber("01012345678");
        entityManager.persist(member);
        entityManager.flush();
        LocalDateTime originalUpdatedAt = member.getUpdatedAt();
        LocalDateTime accessedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        // When
        memberCommandRepository.updateLastAccessedAt(member.getId(), accessedAt);
        entityManager.clear();

        // Then
        Member reloaded = entityManager.find(Member.class, member.getId());
        assertThat(reloaded.getLastAccessedAt()).isEqualTo(accessedAt);
        assertThat(reloaded.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }
}
