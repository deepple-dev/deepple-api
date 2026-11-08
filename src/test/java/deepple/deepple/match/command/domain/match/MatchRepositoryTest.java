package deepple.deepple.match.command.domain.match;

import deepple.deepple.common.support.RepositoryTestSupport;
import deepple.deepple.match.fixture.MatchFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class MatchRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("서로 매치가 존재하는 경우 True 반환.")
    void getTrueWhenMatchExistsBetween() {
        // Given
        Long responderId = 1L;
        Long requesterId = 2L;

        Match match = MatchFixture.builder()
            .requesterId(requesterId)
            .responderId(responderId)
            .build();

        entityManager.persist(match);
        entityManager.flush();

        // When
        boolean result = matchRepository.existsActiveMatchBetween(requesterId, responderId);
        boolean otherResult = matchRepository.existsActiveMatchBetween(responderId, requesterId);

        // Then
        Assertions.assertThat(result).isTrue();
        Assertions.assertThat(otherResult).isTrue();
    }

    @Test
    @DisplayName("매치가 존재하지 않는 경우 False 반환")
    void getFalseWhenMatchNotExistsBetween() {
        // Given
        Long requesterId = 1L;
        Long responderId = 2L;

        // When
        boolean result = matchRepository.existsActiveMatchBetween(requesterId, responderId);
        boolean otherResult = matchRepository.existsActiveMatchBetween(responderId, requesterId);

        // Then
        Assertions.assertThat(result).isFalse();
        Assertions.assertThat(otherResult).isFalse();
    }

    @Test
    @DisplayName("만료된 매치가 존재하는 경우 False 반환")
    void getFalseWhenExpiredMatchExistsBetween() {
        // Given
        Long requesterId = 1L;
        Long responderId = 2L;

        Match match = MatchFixture.builder()
            .requesterId(requesterId)
            .responderId(responderId)
            .status(MatchStatus.EXPIRED)
            .build();

        entityManager.persist(match);
        entityManager.flush();

        // When
        boolean result = matchRepository.existsActiveMatchBetween(requesterId, responderId);
        boolean otherResult = matchRepository.existsActiveMatchBetween(responderId, requesterId);

        // Then
        Assertions.assertThat(result).isFalse();
        Assertions.assertThat(otherResult).isFalse();
    }

    @Test
    @DisplayName("거절 확인이 된 매치가 존재하는 경우, False 반환")
    void getFalseWhenRejectCheckedMatchExistsBetween() {
        // Given
        Long requesterId = 1L;
        Long responderId = 2L;

        Match match = MatchFixture.builder()
            .requesterId(requesterId)
            .responderId(responderId)
            .status(MatchStatus.REJECT_CHECKED)
            .build();

        entityManager.persist(match);
        entityManager.flush();

        // When
        boolean result = matchRepository.existsActiveMatchBetween(requesterId, responderId);
        boolean otherResult = matchRepository.existsActiveMatchBetween(responderId, requesterId);

        // Then
        Assertions.assertThat(result).isFalse();
        Assertions.assertThat(otherResult).isFalse();
    }
}
