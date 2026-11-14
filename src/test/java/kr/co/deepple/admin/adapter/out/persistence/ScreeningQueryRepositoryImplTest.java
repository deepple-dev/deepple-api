package kr.co.deepple.admin.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import kr.co.deepple.admin.application.dto.RawScreeningView;
import kr.co.deepple.admin.domain.Screening;
import kr.co.deepple.admin.domain.ScreeningStatus;
import kr.co.deepple.admin.fixture.ScreeningFixture;
import kr.co.deepple.common.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class ScreeningQueryRepositoryImplTest extends RepositoryTestSupport {

  private static final long MEMBER1_ID = 101L;
  private static final long MEMBER2_ID = 102L;

  @Autowired private TestEntityManager em;

  @Autowired private ScreeningQueryRepositoryImpl screeningQueryRepository;

  @Test
  @DisplayName("조건 없이 전체 심사를 조회합니다.")
  void findAllScreenings() {
    // given
    Screening screening1 = em.persist(ScreeningFixture.builder().memberId(MEMBER1_ID).build());
    Screening screening2 = em.persist(ScreeningFixture.builder().memberId(MEMBER2_ID).build());
    em.flush();
    em.clear();

    // when
    Page<RawScreeningView> screenings =
        screeningQueryRepository.findScreenings(null, null, PageRequest.of(0, 10));

    // then
    assertThat(screenings.getTotalElements()).isEqualTo(2);
    assertThat(screenings.getContent())
        .containsExactlyInAnyOrder(
            new RawScreeningView(screening1.getId(), MEMBER1_ID, "PENDING", null),
            new RawScreeningView(screening2.getId(), MEMBER2_ID, "PENDING", null));
  }

  @Test
  @DisplayName("최근에 만들어진 심사부터 페이지 크기만큼 조회하고 전체 개수는 조건에 맞는 전체를 셉니다.")
  void findScreeningsWithPaging() throws InterruptedException {
    // given
    Screening oldest = em.persistAndFlush(ScreeningFixture.builder().memberId(MEMBER1_ID).build());
    Thread.sleep(5);
    Screening middle = em.persistAndFlush(ScreeningFixture.builder().memberId(MEMBER2_ID).build());
    Thread.sleep(5);
    Screening newest = em.persistAndFlush(ScreeningFixture.builder().memberId(103L).build());
    em.clear();

    // when
    Page<RawScreeningView> firstPage =
        screeningQueryRepository.findScreenings(null, null, PageRequest.of(0, 2));
    Page<RawScreeningView> secondPage =
        screeningQueryRepository.findScreenings(null, null, PageRequest.of(1, 2));

    // then
    assertThat(firstPage.getTotalElements()).isEqualTo(3);
    assertThat(firstPage.getContent())
        .extracting(RawScreeningView::screeningId)
        .containsExactly(newest.getId(), middle.getId());
    assertThat(secondPage.getContent())
        .extracting(RawScreeningView::screeningId)
        .containsExactly(oldest.getId());
  }

  @Test
  @DisplayName("대기 중인 심사를 조회합니다.")
  void findPendingScreenings() {
    // given
    em.persist(ScreeningFixture.builder().memberId(MEMBER1_ID).build());
    em.persist(ScreeningFixture.builder().memberId(MEMBER2_ID).build());
    em.flush();
    em.clear();

    // when
    Page<RawScreeningView> screenings =
        screeningQueryRepository.findScreenings("PENDING", null, PageRequest.of(0, 10));

    // then
    assertThat(screenings.getTotalElements()).isEqualTo(2);
    assertThat(screenings.getContent())
        .extracting(RawScreeningView::memberId)
        .containsExactlyInAnyOrder(MEMBER1_ID, MEMBER2_ID);
  }

  @Test
  @DisplayName("승인된 심사를 조회합니다.")
  void findApprovedScreenings() {
    // given
    em.persist(ScreeningFixture.builder().memberId(MEMBER1_ID).build());
    em.persist(ScreeningFixture.builder().memberId(MEMBER2_ID).build());
    em.flush();
    em.clear();

    // when
    Page<RawScreeningView> screenings =
        screeningQueryRepository.findScreenings("APPROVED", null, PageRequest.of(0, 10));

    // then
    assertThat(screenings.getTotalElements()).isZero();
  }

  @Test
  @DisplayName("회원 번호 조건을 주면 그 회원의 심사만 조회합니다.")
  void findScreeningsByMemberIds() {
    // given
    Screening screening1 = em.persist(ScreeningFixture.builder().memberId(MEMBER1_ID).build());
    em.persist(ScreeningFixture.builder().memberId(MEMBER2_ID).build());
    em.flush();
    em.clear();

    // when
    Page<RawScreeningView> screenings =
        screeningQueryRepository.findScreenings(null, Set.of(MEMBER1_ID), PageRequest.of(0, 10));

    // then
    assertThat(screenings.getTotalElements()).isEqualTo(1);
    assertThat(screenings.getContent().getFirst().screeningId()).isEqualTo(screening1.getId());
    assertThat(screenings.getContent().getFirst().memberId()).isEqualTo(MEMBER1_ID);
  }

  @Test
  @DisplayName("심사 상태와 회원 번호 조건을 함께 설정해 심사를 조회합니다.")
  void findScreenings() {
    // given
    em.persist(ScreeningFixture.builder().memberId(MEMBER1_ID).build());
    em.persist(
        ScreeningFixture.builder().memberId(MEMBER1_ID).status(ScreeningStatus.APPROVED).build());
    em.persist(ScreeningFixture.builder().memberId(MEMBER2_ID).build());
    em.flush();
    em.clear();

    // when
    Page<RawScreeningView> screenings =
        screeningQueryRepository.findScreenings(
            "PENDING", Set.of(MEMBER1_ID), PageRequest.of(0, 10));

    // then
    assertThat(screenings.getTotalElements()).isEqualTo(1);
    assertThat(screenings.getContent().getFirst().screeningStatus()).isEqualTo("PENDING");
    assertThat(screenings.getContent().getFirst().memberId()).isEqualTo(MEMBER1_ID);
    assertThat(screenings.getContent().getFirst().rejectionReason()).isNull();
  }
}
