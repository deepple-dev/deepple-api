package kr.co.deepple.datingexam.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.Map;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.datingexam.domain.DatingExamSubmitResult;
import kr.co.deepple.datingexam.fixture.DatingExamSubmitResultFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class DatingExamSubmitResultRepositoryTest extends RepositoryTestSupport {

  @Autowired private DatingExamSubmitResultRepository datingExamSubmitResultRepository;

  @Autowired private TestEntityManager em;

  @Test
  @DisplayName("주어진 회원들의 연애고사 결과만 조회하고, 결과가 없는 회원과 다른 회원은 포함하지 않는다.")
  void findByMemberIdIn() {
    persist(1L, AnswerPersonalityType.STIMULATING_ADVENTURER);
    persist(2L, AnswerPersonalityType.REALISTIC_SHELTER);
    persist(3L, AnswerPersonalityType.RATIONAL_REALIST);
    em.flush();
    em.clear();

    List<DatingExamSubmitResult> results =
        datingExamSubmitResultRepository.findByMemberIdIn(List.of(1L, 2L, 99L));

    assertThat(results)
        .extracting(
            DatingExamSubmitResult::getMemberId, DatingExamSubmitResult::getDominantPersonalityType)
        .containsExactlyInAnyOrder(
            tuple(1L, AnswerPersonalityType.STIMULATING_ADVENTURER),
            tuple(2L, AnswerPersonalityType.REALISTIC_SHELTER));
  }

  private void persist(long memberId, AnswerPersonalityType type) {
    em.persist(
        DatingExamSubmitResultFixture.builder().memberId(memberId).counts(Map.of(type, 1)).build());
  }
}
