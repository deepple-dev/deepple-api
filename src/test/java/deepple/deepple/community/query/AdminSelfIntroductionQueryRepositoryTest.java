package deepple.deepple.community.query;

import deepple.deepple.common.config.QueryDslConfig;
import deepple.deepple.common.event.Events;
import deepple.deepple.community.command.domain.selfintroduction.SelfIntroduction;
import deepple.deepple.community.presentation.selfintroduction.dto.AdminSelfIntroductionSearchCondition;
import deepple.deepple.community.query.selfintroduction.AdminSelfIntroductionQueryRepository;
import deepple.deepple.community.query.selfintroduction.view.AdminSelfIntroductionView;
import deepple.deepple.member.command.domain.member.Gender;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.vo.MemberProfile;
import deepple.deepple.member.command.domain.member.vo.Nickname;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@DataJpaTest
@Import({QueryDslConfig.class, AdminSelfIntroductionQueryRepository.class})
class AdminSelfIntroductionQueryRepositoryTest {

    private static final int PAGE_SIZE = 5;
    private static MockedStatic<Events> mockedEvents;

    Member manMember;
    Member womanMember;
    List<SelfIntroduction> selfIntroductionsByMan;
    List<SelfIntroduction> selfIntroductionsByWoman;
    @Autowired
    private AdminSelfIntroductionQueryRepository adminSelfIntroductionQueryRepository;
    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        mockedEvents = Mockito.mockStatic(Events.class);
        mockedEvents.when(() -> Events.raise(Mockito.any()))
            .thenAnswer(invocation -> null);

        manMember = Member.fromPhoneNumber("01012345678");
        womanMember = Member.fromPhoneNumber("01098765432");

        entityManager.persist(manMember);
        entityManager.persist(womanMember);

        MemberProfile manProfile = MemberProfile
            .builder()
            .nickname(Nickname.from("남자닉네임"))
            .gender(Gender.MALE)
            .build();

        MemberProfile womanProfile = MemberProfile
            .builder()
            .nickname(Nickname.from("여자닉네임"))
            .gender(Gender.FEMALE)
            .build();

        manMember.updateProfile(manProfile);
        womanMember.updateProfile(womanProfile);

        entityManager.flush();

        selfIntroductionsByMan = new ArrayList<>();
        selfIntroductionsByWoman = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            SelfIntroduction selfIntroductionByMan = SelfIntroduction.write(manMember.getId(), "제목" + i,
                "내용은 30자 이상이어야합니다... 30자를 채우자!! " + i, null);
            SelfIntroduction selfIntroductionByWoman = SelfIntroduction.write(womanMember.getId(), "제목입니다." + i,
                "내용입니다. 내용은 30자 이상이어야합니다... 30자를 채우자!! " + i, null);

            if (i % 2 == 0) {
                selfIntroductionByMan.close();
                selfIntroductionByWoman.close();
            }

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

    @AfterEach
    void tearDown() {
        mockedEvents.close();
    }

    @DisplayName("모든 셀프 소개를 조회합니다.")
    @Test
    void findAllSelfIntroductions() {
        // Given
        AdminSelfIntroductionSearchCondition condition = new AdminSelfIntroductionSearchCondition(
            null,
            null,
            null,
            null,
            null
        );
        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);

        // When
        Page<AdminSelfIntroductionView> selfIntroductionViews = adminSelfIntroductionQueryRepository.findSelfIntroductions(
            condition, pageRequest);

        // Then
        AssertionsForClassTypes.assertThat(selfIntroductionViews.getTotalElements()).isEqualTo(
            selfIntroductionsByWoman.size() + selfIntroductionsByMan.size());
    }

    @Test
    @DisplayName("공개 여부로 셀프 소개를 조회합니다.")
    void findSelfIntroductionsByOpenedFlag() {
        // Given
        AdminSelfIntroductionSearchCondition condition = new AdminSelfIntroductionSearchCondition(
            true,
            null,
            null,
            null,
            null
        );
        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);

        // When
        Page<AdminSelfIntroductionView> selfIntroductionViews = adminSelfIntroductionQueryRepository.findSelfIntroductions(
            condition, pageRequest);

        // Then
        for (AdminSelfIntroductionView view : selfIntroductionViews.getContent()) {
            AssertionsForClassTypes.assertThat(view.isOpened()).isTrue();
        }
    }

    @Test
    @DisplayName("닉네임으로 셀프 소개를 조회합니다.")
    void findSelfIntroductionsByNickname() {
        // Given
        AdminSelfIntroductionSearchCondition condition = new AdminSelfIntroductionSearchCondition(
            null,
            manMember.getProfile().getNickname().getValue(),
            null,
            null,
            null
        );
        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);

        // When
        Page<AdminSelfIntroductionView> selfIntroductionViews = adminSelfIntroductionQueryRepository.findSelfIntroductions(
            condition, pageRequest);

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
        AdminSelfIntroductionSearchCondition condition = new AdminSelfIntroductionSearchCondition(
            null,
            null,
            womanMember.getPhoneNumber(),
            null,
            null
        );
        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);

        // When
        Page<AdminSelfIntroductionView> selfIntroductionViews = adminSelfIntroductionQueryRepository.findSelfIntroductions(
            condition, pageRequest);

        // Then
        AssertionsForClassTypes.assertThat(selfIntroductionViews.getTotalElements()).isEqualTo(PAGE_SIZE);

        for (int i = 0; i < PAGE_SIZE; i++) {
            SelfIntroduction selfIntroductionByWoman = selfIntroductionsByWoman.get(i);
            AdminSelfIntroductionView view = selfIntroductionViews.getContent().get(i);
            AssertionsForClassTypes.assertThat(view.nickname())
                .isEqualTo(womanMember.getProfile().getNickname().getValue());
            AssertionsForClassTypes.assertThat(view.selfIntroductionId())
                .isEqualTo(selfIntroductionByWoman.getId());
            AssertionsForClassTypes.assertThat(view.gender())
                .isEqualTo(womanMember.getProfile().getGender().name());
            AssertionsForClassTypes.assertThat(view.content()).isEqualTo(selfIntroductionByWoman.getContent());
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
        AdminSelfIntroductionSearchCondition condition = new AdminSelfIntroductionSearchCondition(
            null,
            null,
            womanMember.getPhoneNumber(),
            LocalDate.EPOCH,
            LocalDate.now().plusDays(1)
        );
        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);

        // When
        Page<AdminSelfIntroductionView> selfIntroductionViews = adminSelfIntroductionQueryRepository.findSelfIntroductions(
            condition, pageRequest);

        // Then
        AssertionsForClassTypes.assertThat(selfIntroductionViews.getTotalElements()).isEqualTo(PAGE_SIZE);
    }

    private String toDate(LocalDateTime localDateTime) {
        return localDateTime != null ? DateTimeFormatter.ofPattern("yyyy/MM/dd").format(localDateTime) : null;
    }


}
