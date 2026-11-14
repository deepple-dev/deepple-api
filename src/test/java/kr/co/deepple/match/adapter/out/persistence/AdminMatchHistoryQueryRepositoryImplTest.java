package kr.co.deepple.match.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.match.application.dto.MatchHistorySearchCondition;
import kr.co.deepple.match.application.dto.RawMatchHistoryView;
import kr.co.deepple.match.domain.Match;
import kr.co.deepple.match.domain.MatchContactType;
import kr.co.deepple.match.domain.MatchStatus;
import kr.co.deepple.match.fixture.MatchFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class AdminMatchHistoryQueryRepositoryImplTest extends RepositoryTestSupport {

  private static final long REQUESTER_ID = 101L;
  private static final long RESPONDER_ID = 102L;
  private static final long OTHER_ID = 103L;
  private static final MatchHistorySearchCondition NO_CONDITION =
      new MatchHistorySearchCondition(null, null, null, null, null);

  @Autowired private TestEntityManager em;

  @Autowired private AdminMatchHistoryQueryRepositoryImpl adminMatchHistoryQueryRepository;

  @Test
  @DisplayName("조건 없이 전체 매치 내역을 최근 순으로 조회합니다.")
  void findAllMatchHistories() {
    // given
    Match match1 = em.persist(createMatch(REQUESTER_ID, RESPONDER_ID, "안녕하세요"));
    Match match2 = em.persist(createMatch(RESPONDER_ID, OTHER_ID, "반갑습니다"));
    em.flush();
    em.clear();

    // when
    Page<RawMatchHistoryView> matchHistories =
        adminMatchHistoryQueryRepository.findMatchHistories(
            NO_CONDITION, null, null, PageRequest.of(0, 10));

    // then
    assertThat(matchHistories.getTotalElements()).isEqualTo(2);
    assertThat(matchHistories.getContent())
        .extracting(RawMatchHistoryView::matchId)
        .containsExactly(match2.getId(), match1.getId());
    assertThat(matchHistories.getContent())
        .extracting(RawMatchHistoryView::requesterId)
        .containsExactly(RESPONDER_ID, REQUESTER_ID);
    assertThat(matchHistories.getContent())
        .extracting(RawMatchHistoryView::responderId)
        .containsExactly(OTHER_ID, RESPONDER_ID);
  }

  @Test
  @DisplayName("페이지 크기만큼만 조회하고 전체 개수는 조건에 맞는 전체를 셉니다.")
  void findMatchHistoriesWithPaging() {
    // given
    Match match1 = em.persist(createMatch(REQUESTER_ID, RESPONDER_ID, "첫 번째"));
    em.persist(createMatch(REQUESTER_ID, OTHER_ID, "두 번째"));
    em.persist(createMatch(RESPONDER_ID, OTHER_ID, "세 번째"));
    em.flush();
    em.clear();

    // when
    Page<RawMatchHistoryView> matchHistories =
        adminMatchHistoryQueryRepository.findMatchHistories(
            NO_CONDITION, null, null, PageRequest.of(1, 2));

    // then
    assertThat(matchHistories.getTotalElements()).isEqualTo(3);
    assertThat(matchHistories.getContent())
        .extracting(RawMatchHistoryView::matchId)
        .containsExactly(match1.getId());
  }

  @Test
  @DisplayName("매치 상태로 매치 내역을 조회합니다.")
  void findMatchHistoriesByStatus() {
    // given
    em.persist(createMatch(REQUESTER_ID, RESPONDER_ID, "대기 중"));
    em.persist(
        MatchFixture.builder()
            .requesterId(RESPONDER_ID)
            .responderId(OTHER_ID)
            .requestMessage("매칭됨")
            .requesterContactType(MatchContactType.KAKAO)
            .status(MatchStatus.MATCHED)
            .responseMessage("수락합니다")
            .responderContactType(MatchContactType.KAKAO)
            .build());
    em.flush();
    em.clear();

    var condition = new MatchHistorySearchCondition("WAITING", null, null, null, null);

    // when
    var matchHistories =
        adminMatchHistoryQueryRepository.findMatchHistories(
            condition, null, null, PageRequest.of(0, 10));

    // then
    assertThat(matchHistories.getTotalElements()).isEqualTo(1);
    assertThat(matchHistories.getContent().getFirst().status()).isEqualTo("WAITING");
    assertThat(matchHistories.getContent().getFirst().requestMessage()).isEqualTo("대기 중");
  }

  @Test
  @DisplayName("닉네임으로 찾은 회원이 요청자인 매치 내역을 조회합니다.")
  void findMatchHistoriesByRequesterNickname() {
    // given
    Match match1 = em.persist(createMatch(REQUESTER_ID, RESPONDER_ID, "안녕하세요"));
    em.persist(createMatch(OTHER_ID, RESPONDER_ID, "반갑습니다"));
    em.flush();
    em.clear();

    // when
    var matchHistories =
        adminMatchHistoryQueryRepository.findMatchHistories(
            NO_CONDITION, Set.of(REQUESTER_ID), null, PageRequest.of(0, 10));

    // then
    assertThat(matchHistories.getTotalElements()).isEqualTo(1);
    assertThat(matchHistories.getContent().getFirst().matchId()).isEqualTo(match1.getId());
    assertThat(matchHistories.getContent().getFirst().requesterId()).isEqualTo(REQUESTER_ID);
    assertThat(matchHistories.getContent().getFirst().responderId()).isEqualTo(RESPONDER_ID);
  }

  @Test
  @DisplayName("닉네임으로 찾은 회원이 응답자인 매치 내역을 조회합니다.")
  void findMatchHistoriesByResponderNickname() {
    // given
    Match match1 = em.persist(createMatch(REQUESTER_ID, RESPONDER_ID, "안녕하세요"));
    em.persist(createMatch(REQUESTER_ID, OTHER_ID, "반갑습니다"));
    em.flush();
    em.clear();

    // when
    var matchHistories =
        adminMatchHistoryQueryRepository.findMatchHistories(
            NO_CONDITION, Set.of(RESPONDER_ID), null, PageRequest.of(0, 10));

    // then
    assertThat(matchHistories.getTotalElements()).isEqualTo(1);
    assertThat(matchHistories.getContent().getFirst().matchId()).isEqualTo(match1.getId());
    assertThat(matchHistories.getContent().getFirst().requesterId()).isEqualTo(REQUESTER_ID);
    assertThat(matchHistories.getContent().getFirst().responderId()).isEqualTo(RESPONDER_ID);
  }

  @Test
  @DisplayName("전화번호로 찾은 회원이 요청자인 매치 내역을 조회합니다.")
  void findMatchHistoriesByRequesterPhoneNumber() {
    // given
    Match match1 = em.persist(createMatch(REQUESTER_ID, RESPONDER_ID, "안녕하세요"));
    em.persist(createMatch(OTHER_ID, RESPONDER_ID, "반갑습니다"));
    em.flush();
    em.clear();

    // when
    var matchHistories =
        adminMatchHistoryQueryRepository.findMatchHistories(
            NO_CONDITION, null, Set.of(REQUESTER_ID), PageRequest.of(0, 10));

    // then
    assertThat(matchHistories.getTotalElements()).isEqualTo(1);
    assertThat(matchHistories.getContent().getFirst().matchId()).isEqualTo(match1.getId());
    assertThat(matchHistories.getContent().getFirst().requesterId()).isEqualTo(REQUESTER_ID);
  }

  @Test
  @DisplayName("전화번호로 찾은 회원이 응답자인 매치 내역을 조회합니다.")
  void findMatchHistoriesByResponderPhoneNumber() {
    // given
    Match match1 = em.persist(createMatch(REQUESTER_ID, RESPONDER_ID, "안녕하세요"));
    em.persist(createMatch(REQUESTER_ID, OTHER_ID, "반갑습니다"));
    em.flush();
    em.clear();

    // when
    var matchHistories =
        adminMatchHistoryQueryRepository.findMatchHistories(
            NO_CONDITION, null, Set.of(RESPONDER_ID), PageRequest.of(0, 10));

    // then
    assertThat(matchHistories.getTotalElements()).isEqualTo(1);
    assertThat(matchHistories.getContent().getFirst().matchId()).isEqualTo(match1.getId());
    assertThat(matchHistories.getContent().getFirst().responderId()).isEqualTo(RESPONDER_ID);
  }

  @Test
  @DisplayName("닉네임으로 찾은 회원과 전화번호로 찾은 회원이 서로 달라도 둘 다 참여한 매치 내역을 조회합니다.")
  void findMatchHistoriesByNicknameAndPhoneNumberOfDifferentParticipants() {
    // given
    Match match1 = em.persist(createMatch(REQUESTER_ID, RESPONDER_ID, "안녕하세요"));
    em.persist(createMatch(REQUESTER_ID, OTHER_ID, "반갑습니다"));
    em.flush();
    em.clear();

    // when
    var matchHistories =
        adminMatchHistoryQueryRepository.findMatchHistories(
            NO_CONDITION, Set.of(REQUESTER_ID), Set.of(RESPONDER_ID), PageRequest.of(0, 10));

    // then
    assertThat(matchHistories.getTotalElements()).isEqualTo(1);
    assertThat(matchHistories.getContent().getFirst().matchId()).isEqualTo(match1.getId());
  }

  @Test
  @DisplayName("시작일과 종료일로 매치 내역을 조회합니다.")
  void findMatchHistoriesByDateRange() {
    // given
    Match match = em.persist(createMatch(REQUESTER_ID, RESPONDER_ID, "안녕하세요"));
    em.flush();
    em.clear();

    var condition =
        new MatchHistorySearchCondition(
            null, null, null, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
    var outOfRange =
        new MatchHistorySearchCondition(
            null, null, null, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));

    // when
    var matchHistories =
        adminMatchHistoryQueryRepository.findMatchHistories(
            condition, null, null, PageRequest.of(0, 10));
    var emptyHistories =
        adminMatchHistoryQueryRepository.findMatchHistories(
            outOfRange, null, null, PageRequest.of(0, 10));

    // then
    assertThat(matchHistories.getTotalElements()).isEqualTo(1);
    assertThat(matchHistories.getContent().getFirst().matchId()).isEqualTo(match.getId());
    assertThat(emptyHistories.getTotalElements()).isZero();
    assertThat(emptyHistories.getContent()).isEmpty();
  }

  @Test
  @DisplayName("모든 조건으로 매치 내역을 조회합니다.")
  void findMatchHistoriesWithAllConditions() {
    // given
    em.persist(createMatch(REQUESTER_ID, RESPONDER_ID, "안녕하세요"));
    em.flush();
    em.clear();

    var condition =
        new MatchHistorySearchCondition(
            "WAITING",
            "요청자",
            "01011111111",
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(1));

    // when
    var matchHistories =
        adminMatchHistoryQueryRepository.findMatchHistories(
            condition, Set.of(REQUESTER_ID), Set.of(REQUESTER_ID), PageRequest.of(0, 10));

    // then
    assertThat(matchHistories.getTotalElements()).isEqualTo(1);
    assertThat(matchHistories.getContent().getFirst().status()).isEqualTo("WAITING");
    assertThat(matchHistories.getContent().getFirst().requesterId()).isEqualTo(REQUESTER_ID);
    assertThat(matchHistories.getContent().getFirst().responderId()).isEqualTo(RESPONDER_ID);
    assertThat(matchHistories.getContent().getFirst().requestMessage()).isEqualTo("안녕하세요");
  }

  @Test
  @DisplayName("매치 응답 메시지와 읽은 시간을 포함하여 조회합니다.")
  void findMatchHistoriesWithResponseMessage() {
    // given
    em.persist(
        MatchFixture.builder()
            .requesterId(REQUESTER_ID)
            .responderId(RESPONDER_ID)
            .requestMessage("안녕하세요")
            .requesterContactType(MatchContactType.KAKAO)
            .status(MatchStatus.MATCHED)
            .responseMessage("반갑습니다")
            .responderContactType(MatchContactType.KAKAO)
            .readByResponderAt(LocalDateTime.now())
            .build());
    em.flush();
    em.clear();

    var condition = new MatchHistorySearchCondition("MATCHED", null, null, null, null);

    // when
    var matchHistories =
        adminMatchHistoryQueryRepository.findMatchHistories(
            condition, null, null, PageRequest.of(0, 10));

    // then
    assertThat(matchHistories.getTotalElements()).isEqualTo(1);
    var result = matchHistories.getContent().getFirst();
    assertThat(result.status()).isEqualTo("MATCHED");
    assertThat(result.requestMessage()).isEqualTo("안녕하세요");
    assertThat(result.responseMessage()).isEqualTo("반갑습니다");
    assertThat(result.readByResponderAt()).isNotNull();
  }

  private Match createMatch(long requesterId, long responderId, String requestMessage) {
    return MatchFixture.builder()
        .requesterId(requesterId)
        .responderId(responderId)
        .requestMessage(requestMessage)
        .requesterContactType(MatchContactType.KAKAO)
        .build();
  }
}
