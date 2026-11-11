package kr.co.deepple.match.command.domain.match;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.match.fixture.MatchFixture;
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

    @Test
    @DisplayName("진행 중인 매치가 있으면 반대 방향이라도 새 매치를 저장할 수 없다")
    void rejectSecondActiveMatchBetweenSamePair() {
        // Given
        long memberId = 1L;
        long anotherMemberId = 2L;
        matchRepository.save(MatchFixture.builder()
            .requesterId(memberId)
            .responderId(anotherMemberId)
            .build());

        // When & Then
        Assertions.assertThatThrownBy(() -> matchRepository.save(MatchFixture.builder()
                .requesterId(anotherMemberId)
                .responderId(memberId)
                .build()))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MatchErrorCode.MATCH_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("만료되거나 거절 확인된 매치만 있으면 새 매치를 저장할 수 있다")
    void allowNewMatchWhenOnlyFinishedMatchesExist() {
        // Given
        long memberId = 1L;
        long anotherMemberId = 2L;
        matchRepository.save(MatchFixture.builder()
            .requesterId(memberId)
            .responderId(anotherMemberId)
            .status(MatchStatus.EXPIRED)
            .build());
        matchRepository.save(MatchFixture.builder()
            .requesterId(anotherMemberId)
            .responderId(memberId)
            .status(MatchStatus.REJECT_CHECKED)
            .build());

        // When & Then
        Assertions.assertThatCode(() -> matchRepository.save(MatchFixture.builder()
                .requesterId(memberId)
                .responderId(anotherMemberId)
                .build()))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("진행 중이던 매치가 만료되면 새 매치를 저장할 수 있다")
    void allowNewMatchAfterActiveMatchExpires() {
        // Given
        long memberId = 1L;
        long anotherMemberId = 2L;
        Match match = MatchFixture.builder()
            .requesterId(memberId)
            .responderId(anotherMemberId)
            .build();
        matchRepository.save(match);
        match.expire();
        entityManager.flush();

        // When & Then
        Assertions.assertThatCode(() -> matchRepository.save(MatchFixture.builder()
                .requesterId(memberId)
                .responderId(anotherMemberId)
                .build()))
            .doesNotThrowAnyException();
    }
}
