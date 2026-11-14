package kr.co.deepple.community.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.community.application.dto.AdminSelfIntroductionSearchCondition;
import kr.co.deepple.community.application.dto.RawAdminSelfIntroductionView;
import kr.co.deepple.community.domain.SelfIntroduction;
import kr.co.deepple.community.fixture.SelfIntroductionFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class AdminSelfIntroductionQueryRepositoryTest extends RepositoryTestSupport {

  private static final int PAGE_SIZE = 5;
  private static final long MAN_ID = 1L;
  private static final long WOMAN_ID = 2L;
  private static final AdminSelfIntroductionSearchCondition NO_CONDITION =
      new AdminSelfIntroductionSearchCondition(null, null, null, null, null);

  List<SelfIntroduction> selfIntroductionsByMan;
  List<SelfIntroduction> selfIntroductionsByWoman;

  @Autowired private AdminSelfIntroductionQueryRepository adminSelfIntroductionQueryRepository;

  @Autowired private TestEntityManager entityManager;

  @BeforeEach
  void setUp() {
    selfIntroductionsByMan = new ArrayList<>();
    selfIntroductionsByWoman = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      selfIntroductionsByMan.add(
          entityManager.persist(
              SelfIntroductionFixture.builder()
                  .memberId(MAN_ID)
                  .title("제목" + i)
                  .content("내용은 30자 이상이어야합니다... 30자를 채우자!! " + i)
                  .opened(i % 2 == 0)
                  .build()));
      selfIntroductionsByWoman.add(
          entityManager.persist(
              SelfIntroductionFixture.builder()
                  .memberId(WOMAN_ID)
                  .title("제목입니다." + i)
                  .content("내용입니다. 내용은 30자 이상이어야합니다... 30자를 채우자!! " + i)
                  .imageUrl("https://example.com/" + i + ".jpg")
                  .build()));
    }

    selfIntroductionsByMan.sort(Comparator.comparing(SelfIntroduction::getId).reversed());
    selfIntroductionsByWoman.sort(Comparator.comparing(SelfIntroduction::getId).reversed());

    entityManager.flush();
    entityManager.clear();
  }

  @DisplayName("모든 셀프 소개를 최신순으로 조회합니다.")
  @Test
  void findAllSelfIntroductions() {
    // When
    Page<RawAdminSelfIntroductionView> page1 =
        adminSelfIntroductionQueryRepository.findSelfIntroductions(
            NO_CONDITION, null, PageRequest.of(0, PAGE_SIZE));
    Page<RawAdminSelfIntroductionView> page2 =
        adminSelfIntroductionQueryRepository.findSelfIntroductions(
            NO_CONDITION, null, PageRequest.of(1, PAGE_SIZE));

    // Then
    assertThat(page1.getTotalElements())
        .isEqualTo(selfIntroductionsByWoman.size() + selfIntroductionsByMan.size());
    assertThat(page1.getContent()).hasSize(PAGE_SIZE);
    assertThat(page2.getContent()).hasSize(PAGE_SIZE);
    List<Long> ids = new ArrayList<>();
    page1.getContent().forEach(view -> ids.add(view.id()));
    page2.getContent().forEach(view -> ids.add(view.id()));
    assertThat(ids).isSortedAccordingTo(Comparator.reverseOrder()).doesNotHaveDuplicates();
  }

  @Test
  @DisplayName("공개 여부로 셀프 소개를 조회합니다.")
  void findSelfIntroductionsByOpenedFlag() {
    // Given
    AdminSelfIntroductionSearchCondition condition =
        new AdminSelfIntroductionSearchCondition(true, null, null, null, null);

    // When
    Page<RawAdminSelfIntroductionView> views =
        adminSelfIntroductionQueryRepository.findSelfIntroductions(
            condition, null, PageRequest.of(0, PAGE_SIZE));

    // Then
    assertThat(views.getTotalElements()).isEqualTo(3);
    assertThat(views.getContent()).isNotEmpty().allMatch(RawAdminSelfIntroductionView::isOpened);
  }

  @Test
  @DisplayName("회원 번호로 셀프 소개를 조회합니다.")
  void findSelfIntroductionsByMemberIds() {
    // When
    Page<RawAdminSelfIntroductionView> views =
        adminSelfIntroductionQueryRepository.findSelfIntroductions(
            NO_CONDITION, Set.of(WOMAN_ID), PageRequest.of(0, PAGE_SIZE));

    // Then
    assertThat(views.getTotalElements()).isEqualTo(PAGE_SIZE);
    for (int i = 0; i < PAGE_SIZE; i++) {
      SelfIntroduction expected = selfIntroductionsByWoman.get(i);
      RawAdminSelfIntroductionView view = views.getContent().get(i);
      assertThat(view.id()).isEqualTo(expected.getId());
      assertThat(view.memberId()).isEqualTo(WOMAN_ID);
      assertThat(view.isOpened()).isEqualTo(expected.isOpened());
      assertThat(view.content()).isEqualTo(expected.getContent());
      assertThat(view.imageUrl()).isEqualTo(expected.getImageUrl());
      assertThat(view.createdAt()).isNotNull();
      assertThat(view.updatedAt()).isNotNull();
      assertThat(view.deletedAt()).isNull();
    }
  }

  @Test
  @DisplayName("삭제된 셀프 소개도 삭제 일시와 함께 조회합니다.")
  void findDeletedSelfIntroductions() {
    // Given
    SelfIntroduction deleted = selfIntroductionsByMan.getFirst();
    entityManager.find(SelfIntroduction.class, deleted.getId()).delete();
    entityManager.flush();
    entityManager.clear();

    // When
    Page<RawAdminSelfIntroductionView> views =
        adminSelfIntroductionQueryRepository.findSelfIntroductions(
            NO_CONDITION, Set.of(MAN_ID), PageRequest.of(0, PAGE_SIZE));

    // Then
    assertThat(views.getTotalElements()).isEqualTo(selfIntroductionsByMan.size());
    assertThat(views.getContent().getFirst().id()).isEqualTo(deleted.getId());
    assertThat(views.getContent().getFirst().deletedAt()).isNotNull();
  }

  @Test
  @DisplayName("시작일과 종료일을 설정해 셀프 소개를 조회합니다.")
  void findSelfIntroductionsByStartDateAndEndDate() {
    // Given
    AdminSelfIntroductionSearchCondition included =
        new AdminSelfIntroductionSearchCondition(
            null, null, null, LocalDate.EPOCH, LocalDate.now().plusDays(1));
    AdminSelfIntroductionSearchCondition beforeStart =
        new AdminSelfIntroductionSearchCondition(
            null, null, null, LocalDate.now().plusDays(2), null);
    AdminSelfIntroductionSearchCondition afterEnd =
        new AdminSelfIntroductionSearchCondition(
            null, null, null, null, LocalDate.now().minusDays(2));
    PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);

    // When & Then
    assertThat(
            adminSelfIntroductionQueryRepository
                .findSelfIntroductions(included, Set.of(WOMAN_ID), pageRequest)
                .getTotalElements())
        .isEqualTo(PAGE_SIZE);
    assertThat(
            adminSelfIntroductionQueryRepository
                .findSelfIntroductions(beforeStart, null, pageRequest)
                .getTotalElements())
        .isZero();
    assertThat(
            adminSelfIntroductionQueryRepository
                .findSelfIntroductions(afterEnd, null, pageRequest)
                .getTotalElements())
        .isZero();
  }
}
