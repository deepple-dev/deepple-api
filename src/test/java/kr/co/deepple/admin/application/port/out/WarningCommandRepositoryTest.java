package kr.co.deepple.admin.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import kr.co.deepple.admin.application.port.out.WarningCommandRepository.MemberWarningCount;
import kr.co.deepple.admin.fixture.WarningFixture;
import kr.co.deepple.common.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WarningCommandRepositoryTest extends RepositoryTestSupport {

  @Autowired private WarningCommandRepository warningCommandRepository;

  @Test
  @DisplayName("요청한 회원별 경고 수를 세고, 경고가 없는 회원과 요청하지 않은 회원은 결과에 없다")
  void countByMemberIds() {
    // given
    saveWarning(1L, true);
    saveWarning(1L, false);
    saveWarning(2L, true);
    saveWarning(4L, true);

    // when
    List<MemberWarningCount> counts =
        warningCommandRepository.countByMemberIds(List.of(1L, 2L, 3L));

    // then
    assertThat(counts)
        .extracting(MemberWarningCount::getMemberId, MemberWarningCount::getWarningCount)
        .containsExactlyInAnyOrder(tuple(1L, 2L), tuple(2L, 1L));
  }

  private void saveWarning(long memberId, boolean critical) {
    warningCommandRepository.saveAndFlush(
        WarningFixture.builder().adminId(500L).memberId(memberId).critical(critical).build());
  }
}
