package kr.co.deepple.admin.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import kr.co.deepple.admin.application.dto.RawScreeningDetailView;
import kr.co.deepple.admin.domain.RejectionReasonType;
import kr.co.deepple.admin.domain.Screening;
import kr.co.deepple.admin.domain.ScreeningStatus;
import kr.co.deepple.admin.fixture.ScreeningFixture;
import kr.co.deepple.common.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class ScreeningDetailQueryRepositoryTest extends RepositoryTestSupport {

  @Autowired private TestEntityManager em;

  @Autowired private ScreeningDetailQueryRepository screeningDetailQueryRepository;

  @Test
  @DisplayName("상세 심사를 조회합니다.")
  void findById() {
    // given
    Screening screening = em.persist(ScreeningFixture.builder().memberId(101L).build());
    em.flush();
    em.clear();

    // when
    Optional<RawScreeningDetailView> screeningDetail =
        screeningDetailQueryRepository.findById(screening.getId());

    // then
    assertThat(screeningDetail)
        .contains(
            new RawScreeningDetailView(
                screening.getId(), screening.getVersion(), 101L, "PENDING", null));
    assertThat(screening.getVersion()).isNotNull();
  }

  @Test
  @DisplayName("반려된 심사는 반려 사유를 함께 조회합니다.")
  void findRejectedScreening() {
    // given
    RejectionReasonType rejectionReason = RejectionReasonType.STOLEN_IMAGE;
    Screening screening =
        em.persist(
            ScreeningFixture.builder()
                .memberId(101L)
                .status(ScreeningStatus.REJECTED)
                .rejectionReason(rejectionReason)
                .build());
    em.flush();
    em.clear();

    // when
    RawScreeningDetailView screeningDetail =
        screeningDetailQueryRepository.findById(screening.getId()).orElseThrow();

    // then
    assertThat(screeningDetail.screeningStatus()).isEqualTo("REJECTED");
    assertThat(screeningDetail.rejectionReason()).isEqualTo(rejectionReason.name());
  }

  @Test
  @DisplayName("없는 심사는 빈 결과를 돌려줍니다.")
  void findByIdWhenAbsent() {
    assertThat(screeningDetailQueryRepository.findById(-1L)).isEmpty();
  }
}
