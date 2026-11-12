package kr.co.deepple.community.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.community.command.domain.selfintroduction.SelfIntroduction;
import kr.co.deepple.community.fixture.SelfIntroductionFixture;
import kr.co.deepple.community.presentation.selfintroduction.dto.AdminSelfIntroductionSearchCondition;
import kr.co.deepple.community.query.selfintroduction.AdminSelfIntroductionQueryRepository;
import kr.co.deepple.community.query.selfintroduction.view.AdminSelfIntroductionView;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class AdminSelfIntroductionQueryRepositoryTest extends RepositoryTestSupport {

  private static final int PAGE_SIZE = 5;

  Member manMember;
  Member womanMember;
  List<SelfIntroduction> selfIntroductionsByMan;
  List<SelfIntroduction> selfIntroductionsByWoman;

  @Autowired private AdminSelfIntroductionQueryRepository adminSelfIntroductionQueryRepository;

  @Autowired private TestEntityManager entityManager;

  @BeforeEach
  void setUp() {
    manMember =
        MemberFixture.builder()
            .phoneNumber("01012345678")
            .nickname("남자닉네임")
            .gender(Gender.MALE)
            .build();
    womanMember =
        MemberFixture.builder()
            .phoneNumber("01098765432")
            .nickname("여자닉네임")
            .gender(Gender.FEMALE)
            .build();

    entityManager.persist(manMember);
    entityManager.persist(womanMember);
    entityManager.flush();

    selfIntroductionsByMan = new ArrayList<>();
    selfIntroductionsByWoman = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      SelfIntroduction selfIntroductionByMan =
          SelfIntroductionFixture.builder()
              .memberId(manMember.getId())
              .title("제목" + i)
              .content("내용은 30자 이상이어야합니다... 30자를 채우자!! " + i)
              .opened(i % 2 == 0)
              .build();
      SelfIntroduction selfIntroductionByWoman =
          SelfIntroductionFixture.builder()
              .memberId(womanMember.getId())
              .title("제목입니다." + i)
              .content("내용입니다. 내용은 30자 이상이어야합니다... 30자를 채우자!! " + i)
              .build();

      entityManager.persist(selfIntroductionByMan);
      entityManager.persist(selfIntroductionByWoman);

      selfIntroductionsByMan.add(selfIntroductionByMan);
      selfIntroductionsByWoman.add(selfIntroductionByWoman);
    }

    selfIntroductionsByMan.sort(Comparator.comparing(SelfIntroduction::getId).reversed());
    selfIntroductionsByWoman.sort(Comparator.comparing(SelfIntroduction::getId).reversed());

    entityManager.flush();
    entityManager.clear();
  }

  @DisplayName("모든 셀프 소개를 조회합니다.")
  @Test
  void findAllSelfIntroductions() {
    // Given
    AdminSelfIntroductionSearchCondition condition =
        new AdminSelfIntroductionSearchCondition(null, null, null, null, null);
    PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);

    // When
    Page<AdminSelfIntroductionView> selfIntroductionViews =
        adminSelfIntroductionQueryRepository.findSelfIntroductions(condition, pageRequest);

    // Then
    AssertionsForClassTypes.assertThat(selfIntroductionViews.getTotalElements())
        .isEqualTo(selfIntroductionsByWoman.size() + selfIntroductionsByMan.size());
  }

  @Test
  @DisplayName("공개 여부로 셀프 소개를 조회합니다.")
  void findSelfIntroductionsByOpenedFlag() {
    // Given
    AdminSelfIntroductionSearchCondition condition =
        new AdminSelfIntroductionSearchCondition(true, null, null, null, null);
    PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);

    // When
    Page<AdminSelfIntroductionView> selfIntroductionViews =
        adminSelfIntroductionQueryRepository.findSelfIntroductions(condition, pageRequest);

    // Then
    AssertionsForClassTypes.assertThat(selfIntroductionViews.getContent().isEmpty()).isFalse();
    for (AdminSelfIntroductionView view : selfIntroductionViews.getContent()) {
      AssertionsForClassTypes.assertThat(view.isOpened()).isTrue();
    }
  }

  @Test
  @DisplayName("닉네임으로 셀프 소개를 조회합니다.")
  void findSelfIntroductionsByNickname() {
    // Given
    AdminSelfIntroductionSearchCondition condition =
        new AdminSelfIntroductionSearchCondition(
            null, manMember.getProfile().getNickname().getValue(), null, null, null);
    PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);

    // When
    Page<AdminSelfIntroductionView> selfIntroductionViews =
        adminSelfIntroductionQueryRepository.findSelfIntroductions(condition, pageRequest);

    // Then
    AssertionsForClassTypes.assertThat(selfIntroductionViews.getTotalElements())
        .isEqualTo(selfIntroductionsByMan.size());
    for (AdminSelfIntroductionView view : selfIntroductionViews.getContent()) {
      AssertionsForClassTypes.assertThat(view.nickname())
          .isEqualTo(manMember.getProfile().getNickname().getValue());
    }
  }

  @Test
  @DisplayName("전화번호로 셀프 소개를 조회합니다.")
  void findSelfIntroductionsByPhoneNumber() {
    // Given
    AdminSelfIntroductionSearchCondition condition =
        new AdminSelfIntroductionSearchCondition(
            null, null, womanMember.getPhoneNumber(), null, null);
    PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);

    // When
    Page<AdminSelfIntroductionView> selfIntroductionViews =
        adminSelfIntroductionQueryRepository.findSelfIntroductions(condition, pageRequest);

    // Then
    AssertionsForClassTypes.assertThat(selfIntroductionViews.getTotalElements())
        .isEqualTo(PAGE_SIZE);

    for (int i = 0; i < PAGE_SIZE; i++) {
      SelfIntroduction selfIntroductionByWoman = selfIntroductionsByWoman.get(i);
      AdminSelfIntroductionView view = selfIntroductionViews.getContent().get(i);
      AssertionsForClassTypes.assertThat(view.nickname())
          .isEqualTo(womanMember.getProfile().getNickname().getValue());
      AssertionsForClassTypes.assertThat(view.selfIntroductionId())
          .isEqualTo(selfIntroductionByWoman.getId());
      AssertionsForClassTypes.assertThat(view.gender())
          .isEqualTo(womanMember.getProfile().getGender().name());
      AssertionsForClassTypes.assertThat(view.content())
          .isEqualTo(selfIntroductionByWoman.getContent());
      AssertionsForClassTypes.assertThat(view.createdDate())
          .isEqualTo(toDate(selfIntroductionByWoman.getCreatedAt()));
      AssertionsForClassTypes.assertThat(view.updatedDate())
          .isEqualTo(toDate(selfIntroductionByWoman.getUpdatedAt()));
      AssertionsForClassTypes.assertThat(view.deletedDate())
          .isEqualTo(toDate(selfIntroductionByWoman.getDeletedAt()));
    }
  }

  @Test
  @DisplayName("시작일과 종료일을 설정해 셀프 소개를 조회합니다.")
  void findSelfIntroductionsByStartDateAndEndDate() {
    // Given
    AdminSelfIntroductionSearchCondition condition =
        new AdminSelfIntroductionSearchCondition(
            null, null, womanMember.getPhoneNumber(), LocalDate.EPOCH, LocalDate.now().plusDays(1));
    PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);

    // When
    Page<AdminSelfIntroductionView> selfIntroductionViews =
        adminSelfIntroductionQueryRepository.findSelfIntroductions(condition, pageRequest);

    // Then
    AssertionsForClassTypes.assertThat(selfIntroductionViews.getTotalElements())
        .isEqualTo(PAGE_SIZE);
  }

  private String toDate(LocalDateTime localDateTime) {
    return localDateTime != null
        ? DateTimeFormatter.ofPattern("yyyy/MM/dd").format(localDateTime)
        : null;
  }
}
