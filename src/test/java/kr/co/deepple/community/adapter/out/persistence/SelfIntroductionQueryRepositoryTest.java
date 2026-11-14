package kr.co.deepple.community.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.community.application.dto.RawSelfIntroductionView;
import kr.co.deepple.community.domain.ProfileExchangeStatus;
import kr.co.deepple.community.domain.SelfIntroduction;
import kr.co.deepple.community.fixture.ProfileExchangeFixture;
import kr.co.deepple.community.fixture.SelfIntroductionFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class SelfIntroductionQueryRepositoryTest extends RepositoryTestSupport {

  private static final long MEMBER_ID = 1L;
  private static final long OTHER_MEMBER_ID = 2L;
  private static final int LIMIT = 50;

  @Autowired private SelfIntroductionQueryRepository selfIntroductionQueryRepository;

  @Autowired private TestEntityManager entityManager;

  @Nested
  @DisplayName("셀프 소개 목록 조회 테스트")
  class SelfIntroductionListTest {

    List<SelfIntroduction> selfIntroductions = new ArrayList<>();

    @BeforeEach
    void setUp() {
      for (int i = 0; i < 6; i++) {
        selfIntroductions.add(persist(MEMBER_ID, "제목" + i, i % 2 == 0));
        selfIntroductions.add(persist(OTHER_MEMBER_ID, "제목" + i, i % 2 == 0));
      }
      entityManager.flush();
      selfIntroductions.sort(Comparator.comparing(SelfIntroduction::getId).reversed());
    }

    @Test
    @DisplayName("자신의 글 또는 공개된 글을 최신순으로 불러온다.")
    void findOpenedOrMine() {
      // given
      List<Long> expectedIds =
          selfIntroductions.stream()
              .filter(s -> s.isOpened() || s.getMemberId() == MEMBER_ID)
              .map(SelfIntroduction::getId)
              .toList();

      // when
      List<RawSelfIntroductionView> views =
          selfIntroductionQueryRepository.findSelfIntroductions(MEMBER_ID, null, Set.of(), LIMIT);

      // then
      assertThat(views)
          .extracting(RawSelfIntroductionView::id)
          .containsExactlyElementsOf(expectedIds);
      RawSelfIntroductionView first = views.getFirst();
      SelfIntroduction expected =
          selfIntroductions.stream()
              .filter(s -> s.getId().equals(first.id()))
              .findFirst()
              .orElseThrow();
      assertThat(first.memberId()).isEqualTo(expected.getMemberId());
      assertThat(first.title()).isEqualTo(expected.getTitle());
      assertThat(first.content()).isEqualTo(expected.getContent());
      assertThat(first.imageUrl()).isEqualTo(expected.getImageUrl());
      assertThat(first.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("다른 회원의 비공개 글은 불러오지 않는다.")
    void excludeClosedOfOthers() {
      // when
      List<RawSelfIntroductionView> views =
          selfIntroductionQueryRepository.findSelfIntroductions(999L, null, Set.of(), LIMIT);

      // then
      assertThat(views)
          .extracting(RawSelfIntroductionView::id)
          .containsExactlyElementsOf(
              selfIntroductions.stream()
                  .filter(SelfIntroduction::isOpened)
                  .map(SelfIntroduction::getId)
                  .toList());
    }

    @Test
    @DisplayName("lastId보다 작은 id의 글을 limit만큼 불러온다.")
    void findWithCursorAndLimit() {
      // given
      List<Long> visibleIds =
          selfIntroductions.stream()
              .filter(s -> s.isOpened() || s.getMemberId() == MEMBER_ID)
              .map(SelfIntroduction::getId)
              .toList();

      // when
      List<RawSelfIntroductionView> page1 =
          selfIntroductionQueryRepository.findSelfIntroductions(MEMBER_ID, null, Set.of(), 4);
      List<RawSelfIntroductionView> page2 =
          selfIntroductionQueryRepository.findSelfIntroductions(
              MEMBER_ID, page1.getLast().id(), Set.of(), LIMIT);

      // then
      assertThat(page1)
          .extracting(RawSelfIntroductionView::id)
          .containsExactlyElementsOf(visibleIds.subList(0, 4));
      assertThat(page2)
          .extracting(RawSelfIntroductionView::id)
          .containsExactlyElementsOf(visibleIds.subList(4, visibleIds.size()));
    }

    @Test
    @DisplayName("차단한 회원의 글은 불러오지 않는다.")
    void excludeBlockedMembers() {
      // when
      List<RawSelfIntroductionView> views =
          selfIntroductionQueryRepository.findSelfIntroductions(
              MEMBER_ID, null, Set.of(OTHER_MEMBER_ID), LIMIT);

      // then
      assertThat(views).isNotEmpty().allMatch(view -> view.memberId() == MEMBER_ID);
    }

    @Test
    @DisplayName("삭제된 글은 불러오지 않는다.")
    void excludeDeleted() {
      // given
      SelfIntroduction deleted = selfIntroductions.getFirst();
      entityManager.find(SelfIntroduction.class, deleted.getId()).delete();
      entityManager.flush();

      // when
      List<RawSelfIntroductionView> views =
          selfIntroductionQueryRepository.findSelfIntroductions(
              deleted.getMemberId(), null, Set.of(), LIMIT);
      List<RawSelfIntroductionView> myViews =
          selfIntroductionQueryRepository.findMySelfIntroductions(
              deleted.getMemberId(), null, LIMIT);

      // then
      assertThat(views).extracting(RawSelfIntroductionView::id).doesNotContain(deleted.getId());
      assertThat(myViews).extracting(RawSelfIntroductionView::id).doesNotContain(deleted.getId());
    }

    @Test
    @DisplayName("자신의 글만 공개 여부와 상관없이 최신순으로 불러온다.")
    void findMine() {
      // given
      List<Long> myIds =
          selfIntroductions.stream()
              .filter(s -> s.getMemberId() == MEMBER_ID)
              .map(SelfIntroduction::getId)
              .toList();

      // when
      List<RawSelfIntroductionView> page1 =
          selfIntroductionQueryRepository.findMySelfIntroductions(MEMBER_ID, null, 4);
      List<RawSelfIntroductionView> page2 =
          selfIntroductionQueryRepository.findMySelfIntroductions(
              MEMBER_ID, page1.getLast().id(), 4);

      // then
      assertThat(page1)
          .extracting(RawSelfIntroductionView::id)
          .containsExactlyElementsOf(myIds.subList(0, 4));
      assertThat(page2)
          .extracting(RawSelfIntroductionView::id)
          .containsExactlyElementsOf(myIds.subList(4, myIds.size()));
    }
  }

  @Nested
  @DisplayName("셀프 소개 상세 조회 테스트")
  class SelfIntroductionFindTest {

    @Test
    @DisplayName("셀프 소개를 공개 여부와 상관없이 조회합니다.")
    void findSelfIntroduction() {
      // given
      SelfIntroduction selfIntroduction = persist(OTHER_MEMBER_ID, "제목", false);
      entityManager.flush();

      // when
      RawSelfIntroductionView view =
          selfIntroductionQueryRepository
              .findSelfIntroduction(selfIntroduction.getId())
              .orElseThrow();

      // then
      assertThat(view.id()).isEqualTo(selfIntroduction.getId());
      assertThat(view.memberId()).isEqualTo(OTHER_MEMBER_ID);
      assertThat(view.title()).isEqualTo(selfIntroduction.getTitle());
      assertThat(view.content()).isEqualTo(selfIntroduction.getContent());
      assertThat(view.imageUrl()).isEqualTo(selfIntroduction.getImageUrl());
      assertThat(view.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 셀프 소개의 경우, 빈 값을 반환합니다.")
    void getEmptyValueWhenSelfIntroductionIsNotExists() {
      assertThat(selfIntroductionQueryRepository.findSelfIntroduction(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    @DisplayName("삭제된 셀프 소개의 경우, 빈 값을 반환합니다.")
    void getEmptyValueWhenSelfIntroductionIsDeleted() {
      // given
      SelfIntroduction selfIntroduction = persist(OTHER_MEMBER_ID, "제목", true);
      selfIntroduction.delete();
      entityManager.flush();

      // when & then
      assertThat(selfIntroductionQueryRepository.findSelfIntroduction(selfIntroduction.getId()))
          .isEmpty();
    }
  }

  @Nested
  @DisplayName("프로필 교환 상태 조회 테스트")
  class ProfileExchangeStatusTest {

    @Test
    @DisplayName("두 회원 사이의 프로필 교환 상태를 요청 방향과 상관없이 조회합니다.")
    void findProfileExchangeStatusBetween() {
      // given
      entityManager.persist(
          ProfileExchangeFixture.builder()
              .requesterId(MEMBER_ID)
              .responderId(OTHER_MEMBER_ID)
              .status(ProfileExchangeStatus.APPROVE)
              .build());
      entityManager.flush();

      // when & then
      assertThat(
              selfIntroductionQueryRepository.findProfileExchangeStatus(MEMBER_ID, OTHER_MEMBER_ID))
          .contains(ProfileExchangeStatus.APPROVE.name());
      assertThat(
              selfIntroductionQueryRepository.findProfileExchangeStatus(OTHER_MEMBER_ID, MEMBER_ID))
          .contains(ProfileExchangeStatus.APPROVE.name());
    }

    @Test
    @DisplayName("두 회원 사이에 프로필 교환이 없으면 빈 값을 반환합니다.")
    void getEmptyValueWhenNoProfileExchange() {
      // given
      entityManager.persist(
          ProfileExchangeFixture.builder().requesterId(MEMBER_ID).responderId(3L).build());
      entityManager.flush();

      // when & then
      assertThat(
              selfIntroductionQueryRepository.findProfileExchangeStatus(MEMBER_ID, OTHER_MEMBER_ID))
          .isEmpty();
    }
  }

  private SelfIntroduction persist(long memberId, String title, boolean opened) {
    return entityManager.persist(
        SelfIntroductionFixture.builder().memberId(memberId).title(title).opened(opened).build());
  }
}
