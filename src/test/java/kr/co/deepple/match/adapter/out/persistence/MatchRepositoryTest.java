package kr.co.deepple.match.adapter.out.persistence;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.match.application.port.out.MatchRepository;
import kr.co.deepple.match.domain.Match;
import kr.co.deepple.match.domain.MatchContactType;
import kr.co.deepple.match.domain.MatchErrorCode;
import kr.co.deepple.match.domain.MatchStatus;
import kr.co.deepple.match.fixture.MatchFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class MatchRepositoryTest extends RepositoryTestSupport {

  @Autowired private MatchRepository matchRepository;

  @Autowired private TestEntityManager entityManager;

  @Test
  @DisplayName("서로 매치가 존재하는 경우 True 반환.")
  void getTrueWhenMatchExistsBetween() {
    // Given
    Long responderId = 1L;
    Long requesterId = 2L;

    Match match = MatchFixture.builder().requesterId(requesterId).responderId(responderId).build();

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

    Match match =
        MatchFixture.builder()
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

    Match match =
        MatchFixture.builder()
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
    matchRepository.save(
        MatchFixture.builder().requesterId(memberId).responderId(anotherMemberId).build());

    // When & Then
    Assertions.assertThatThrownBy(() ->
            matchRepository.save(
                MatchFixture.builder().requesterId(anotherMemberId).responderId(memberId).build()))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MatchErrorCode.MATCH_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("만료되거나 거절 확인된 매치만 있으면 새 매치를 저장할 수 있다")
  void allowNewMatchWhenOnlyFinishedMatchesExist() {
    // Given
    long memberId = 1L;
    long anotherMemberId = 2L;
    matchRepository.save(
        MatchFixture.builder()
            .requesterId(memberId)
            .responderId(anotherMemberId)
            .status(MatchStatus.EXPIRED)
            .build());
    matchRepository.save(
        MatchFixture.builder()
            .requesterId(anotherMemberId)
            .responderId(memberId)
            .status(MatchStatus.REJECT_CHECKED)
            .build());

    // When & Then
    Assertions.assertThatCode(() ->
            matchRepository.save(
                MatchFixture.builder().requesterId(memberId).responderId(anotherMemberId).build()))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("진행 중이던 매치가 만료되면 새 매치를 저장할 수 있다")
  void allowNewMatchAfterActiveMatchExpires() {
    // Given
    long memberId = 1L;
    long anotherMemberId = 2L;
    Match match = MatchFixture.builder().requesterId(memberId).responderId(anotherMemberId).build();
    matchRepository.save(match);
    match.expire();
    entityManager.flush();

    // When & Then
    Assertions.assertThatCode(() ->
            matchRepository.save(
                MatchFixture.builder().requesterId(memberId).responderId(anotherMemberId).build()))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("매칭 상태와 관계없이 나에게 요청한 회원과 내가 요청한 회원을 방향별로 찾는다.")
  void findCounterpartIdsByDirection() {
    long memberId = 100L;
    entityManager.persist(MatchFixture.builder().requesterId(101L).responderId(memberId).build());
    entityManager.persist(
        MatchFixture.builder()
            .requesterId(102L)
            .responderId(memberId)
            .status(MatchStatus.EXPIRED)
            .build());
    entityManager.persist(
        MatchFixture.builder()
            .requesterId(memberId)
            .responderId(103L)
            .status(MatchStatus.REJECT_CHECKED)
            .build());
    entityManager.persist(MatchFixture.builder().requesterId(104L).responderId(105L).build());
    entityManager.flush();

    Assertions.assertThat(matchRepository.findRequesterIdsByResponderId(memberId))
        .containsExactlyInAnyOrder(101L, 102L);
    Assertions.assertThat(matchRepository.findResponderIdsByRequesterId(memberId))
        .containsExactly(103L);
  }

  @ParameterizedTest
  @EnumSource(
      value = MatchStatus.class,
      names = {"WAITING", "MATCHED", "REJECTED"})
  @DisplayName("만료·거절 확인이 아닌 매치는 요청 방향과 관계없이 두 회원 사이의 매치로 찾는다.")
  void findActiveMatchBetween(MatchStatus status) {
    long requesterId = 1L;
    long responderId = 2L;
    Match match =
        entityManager.persist(
            MatchFixture.builder()
                .requesterId(requesterId)
                .responderId(responderId)
                .requestMessage("매치 신청합니다.")
                .status(status)
                .requesterContactType(MatchContactType.PHONE_NUMBER)
                .build());
    entityManager.persist(MatchFixture.builder().requesterId(requesterId).responderId(3L).build());
    entityManager.flush();
    entityManager.clear();

    Assertions.assertThat(matchRepository.findActiveMatchBetween(requesterId, responderId))
        .get()
        .extracting(Match::getId, Match::getRequesterId, Match::getResponderId, Match::getStatus)
        .containsExactly(match.getId(), requesterId, responderId, status);
    Assertions.assertThat(matchRepository.findActiveMatchBetween(responderId, requesterId))
        .get()
        .extracting(Match::getId)
        .isEqualTo(match.getId());
  }

  @ParameterizedTest
  @EnumSource(
      value = MatchStatus.class,
      names = {"EXPIRED", "REJECT_CHECKED"})
  @DisplayName("만료되거나 거절 확인된 매치는 두 회원 사이의 매치로 찾지 않는다.")
  void notFindFinishedMatchBetween(MatchStatus status) {
    long requesterId = 1L;
    long responderId = 2L;
    entityManager.persist(
        MatchFixture.builder()
            .requesterId(requesterId)
            .responderId(responderId)
            .status(status)
            .build());
    entityManager.flush();

    Assertions.assertThat(matchRepository.findActiveMatchBetween(requesterId, responderId))
        .isEmpty();
    Assertions.assertThat(matchRepository.findActiveMatchBetween(responderId, requesterId))
        .isEmpty();
  }

  @Test
  @DisplayName("끝난 매치와 진행 중인 매치가 함께 있으면 진행 중인 매치만 찾는다.")
  void findOnlyActiveMatchAmongFinishedMatches() {
    long memberId = 1L;
    long anotherMemberId = 2L;
    entityManager.persist(
        MatchFixture.builder()
            .requesterId(memberId)
            .responderId(anotherMemberId)
            .status(MatchStatus.EXPIRED)
            .build());
    entityManager.persist(
        MatchFixture.builder()
            .requesterId(anotherMemberId)
            .responderId(memberId)
            .status(MatchStatus.REJECT_CHECKED)
            .build());
    Match activeMatch =
        entityManager.persist(
            MatchFixture.builder()
                .requesterId(anotherMemberId)
                .responderId(memberId)
                .status(MatchStatus.MATCHED)
                .build());
    entityManager.flush();

    Assertions.assertThat(matchRepository.findActiveMatchBetween(memberId, anotherMemberId))
        .get()
        .extracting(Match::getId)
        .isEqualTo(activeMatch.getId());
  }

  @ParameterizedTest
  @EnumSource(
      value = MatchStatus.class,
      names = {"WAITING", "MATCHED", "EXPIRED"})
  @DisplayName("거절·거절 확인이 아닌 매치 요청을 받았으면 받은 요청이 있다고 본다.")
  void existsReceivedMatchRequest(MatchStatus status) {
    long responderId = 1L;
    long requesterId = 2L;
    entityManager.persist(
        MatchFixture.builder()
            .requesterId(requesterId)
            .responderId(responderId)
            .status(status)
            .build());
    entityManager.flush();

    Assertions.assertThat(matchRepository.existsReceivedMatchRequest(responderId, requesterId))
        .isTrue();
  }

  @ParameterizedTest
  @EnumSource(
      value = MatchStatus.class,
      names = {"REJECTED", "REJECT_CHECKED"})
  @DisplayName("거절했거나 거절 확인된 매치 요청은 받은 요청으로 보지 않는다.")
  void notExistsReceivedMatchRequestWhenRejected(MatchStatus status) {
    long responderId = 1L;
    long requesterId = 2L;
    entityManager.persist(
        MatchFixture.builder()
            .requesterId(requesterId)
            .responderId(responderId)
            .status(status)
            .build());
    entityManager.flush();

    Assertions.assertThat(matchRepository.existsReceivedMatchRequest(responderId, requesterId))
        .isFalse();
  }

  @Test
  @DisplayName("내가 보낸 매치 요청이나 다른 회원이 보낸 요청은 받은 요청으로 보지 않는다.")
  void notExistsReceivedMatchRequestWhenOnlySentOrFromOthers() {
    long memberId = 1L;
    long anotherMemberId = 2L;
    entityManager.persist(
        MatchFixture.builder().requesterId(memberId).responderId(anotherMemberId).build());
    entityManager.persist(MatchFixture.builder().requesterId(3L).responderId(memberId).build());
    entityManager.flush();

    Assertions.assertThat(matchRepository.existsReceivedMatchRequest(memberId, anotherMemberId))
        .isFalse();
  }

  @Test
  @DisplayName("같은 회원에게 받은 만료된 요청이 여러 개여도 받은 요청이 있다고 본다.")
  void existsReceivedMatchRequestWithMultipleExpiredMatches() {
    long responderId = 1L;
    long requesterId = 2L;
    for (int i = 0; i < 2; i++) {
      entityManager.persist(
          MatchFixture.builder()
              .requesterId(requesterId)
              .responderId(responderId)
              .status(MatchStatus.EXPIRED)
              .build());
    }
    entityManager.flush();

    Assertions.assertThat(matchRepository.existsReceivedMatchRequest(responderId, requesterId))
        .isTrue();
  }
}
