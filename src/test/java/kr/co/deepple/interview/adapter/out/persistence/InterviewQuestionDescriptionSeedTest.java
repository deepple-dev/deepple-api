package kr.co.deepple.interview.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import kr.co.deepple.common.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class InterviewQuestionDescriptionSeedTest extends RepositoryTestSupport {

  @Autowired private TestEntityManager entityManager;

  @Test
  @DisplayName("기본 인터뷰 질문 31개 모두 설명이 있다.")
  void everySeedQuestionHasDescription() {
    Object withoutDescription =
        entityManager
            .getEntityManager()
            .createNativeQuery("select count(*) from interview_questions where description is null")
            .getSingleResult();

    assertThat(((Number) withoutDescription).longValue()).isZero();
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "현재 하고 있는 일 | 어떤 일을 하는지, 일하며 즐거운 순간을 적어 보세요",
        "일상에서 느끼는 나의 소소한 행복 | 요즘 나를 웃게 하는 작은 순간을 적어 보세요",
        "나의 인간관계 스타일 | 친구나 동료와 어떻게 지내는 편인지 적어 보세요",
        "연인과 함께 가보고 싶은 장소 | 연인과 꼭 가 보고 싶은 곳과 이유를 적어 보세요"
      })
  @DisplayName("리디자인 캔버스에 있는 설명이 그대로 들어 있다.")
  void seedDescription(String content, String description) {
    Object saved =
        entityManager
            .getEntityManager()
            .createNativeQuery("select description from interview_questions where content = ?1")
            .setParameter(1, content)
            .getSingleResult();

    assertThat(saved).isEqualTo(description);
  }
}
