package deepple.deepple.match.command.domain.match;

import deepple.deepple.common.support.RepositoryTestSupport;
import deepple.deepple.match.command.domain.match.vo.Message;
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
        String requestMessage = "매치를 신청합니다.";
        MatchType type = MatchType.MATCH;
        MatchContactType contactType = MatchContactType.PHONE_NUMBER;

        Match match = Match.request(requesterId, responderId, Message.from(requestMessage), "testUser", type,
            contactType);

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
        String requestMessage = "매치를 신청합니다.";
        MatchType type = MatchType.MATCH;
        MatchContactType contactType = MatchContactType.PHONE_NUMBER;

        Match match = Match.request(requesterId, responderId, Message.from(requestMessage), "testUser", type,
            contactType);
        match.expire();

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
        String requestMessage = "매치를 신청합니다.";
        MatchType type = MatchType.MATCH;
        MatchContactType contactType = MatchContactType.PHONE_NUMBER;

        Match match = Match.request(requesterId, responderId, Message.from(requestMessage), "testUser", type,
            contactType);
        match.reject("testUser");
        match.checkRejected();

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
