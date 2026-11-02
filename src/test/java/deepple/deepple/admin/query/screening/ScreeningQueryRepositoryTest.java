package deepple.deepple.admin.query.screening;

import deepple.deepple.admin.command.domain.screening.Screening;
import deepple.deepple.common.support.RepositoryTestSupport;
import deepple.deepple.member.command.domain.member.Gender;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.vo.MemberProfile;
import deepple.deepple.member.command.domain.member.vo.Nickname;
import deepple.deepple.member.command.domain.member.vo.PhoneNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ScreeningQueryRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ScreeningQueryRepository screeningQueryRepository;

    @Test
    @DisplayName("조건 없이 전체 심사를 조회합니다.")
    void findAllScreenings() {
        // given
        Member member1 = createMember("member1", "01011111111");
        Member member2 = createMember("member2", "01022222222");
        Member member3 = createMember("member3", "01033333333");
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);

        Screening screening1 = Screening.from(member1.getId());
        Screening screening2 = Screening.from(member2.getId());
        em.persist(screening1);
        em.persist(screening2);

        em.flush();
        em.clear();

        ScreeningSearchCondition condition = new ScreeningSearchCondition(
            null,
            null,
            null,
            null,
            null
        );
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<ScreeningView> screeningMembers = screeningQueryRepository.findScreenings(condition, pageRequest);

        // then
        assertThat(screeningMembers.getTotalElements()).isEqualTo(2);
        assertThat(screeningMembers.getContent()).extracting("nickname")
            .containsExactlyInAnyOrder("member1", "member2");
    }

    @Test
    @DisplayName("대기 중인 심사를 조회합니다.")
    void findPendingScreenings() {
        // given
        Member member1 = createMember("member1", "01011111111");
        Member member2 = createMember("member2", "01022222222");
        em.persist(member1);
        em.persist(member2);

        Screening screening1 = Screening.from(member1.getId());
        Screening screening2 = Screening.from(member2.getId());
        em.persist(screening1);
        em.persist(screening2);

        em.flush();
        em.clear();

        ScreeningSearchCondition condition = new ScreeningSearchCondition(
            "PENDING",
            null,
            null,
            null,
            null
        );
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<ScreeningView> screeningMembers = screeningQueryRepository.findScreenings(condition, pageRequest);

        // then
        assertThat(screeningMembers.getTotalElements()).isEqualTo(2);
        assertThat(screeningMembers.getContent()).extracting("nickname")
            .containsExactlyInAnyOrder("member1", "member2");
    }

    @Test
    @DisplayName("승인된 심사를 조회합니다.")
    void findApprovedScreenings() {
        // given
        Member member1 = createMember("member1", "01011111111");
        Member member2 = createMember("member2", "01022222222");
        em.persist(member1);
        em.persist(member2);

        Screening screening1 = Screening.from(member1.getId());
        Screening screening2 = Screening.from(member2.getId());
        em.persist(screening1);
        em.persist(screening2);

        em.flush();
        em.clear();

        ScreeningSearchCondition condition = new ScreeningSearchCondition(
            "APPROVED",
            null,
            null,
            null,
            null
        );
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<ScreeningView> screeningMembers = screeningQueryRepository.findScreenings(condition, pageRequest);

        // then
        assertThat(screeningMembers.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("닉네임으로 심사를 조회합니다.")
    void findScreeningsByNickname() {
        // given
        Member member1 = createMember("member1", "01011111111");
        Member member2 = createMember("member2", "01022222222");
        em.persist(member1);
        em.persist(member2);

        Screening screening1 = Screening.from(member1.getId());
        Screening screening2 = Screening.from(member2.getId());
        em.persist(screening1);
        em.persist(screening2);

        em.flush();
        em.clear();

        ScreeningSearchCondition condition = new ScreeningSearchCondition(
            null,
            "member1",
            null,
            null,
            null
        );
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<ScreeningView> screeningMembers = screeningQueryRepository.findScreenings(condition, pageRequest);

        // then
        assertThat(screeningMembers.getTotalElements()).isEqualTo(1);
        assertThat(screeningMembers.getContent().getFirst().nickname()).isEqualTo("member1");
    }

    @Test
    @DisplayName("전화번호로 심사를 조회합니다.")
    void findScreeningsByPhoneNumber() {
        // given
        Member member1 = createMember("member1", "01011111111");
        Member member2 = createMember("member2", "01022222222");
        em.persist(member1);
        em.persist(member2);

        Screening screening1 = Screening.from(member1.getId());
        Screening screening2 = Screening.from(member2.getId());
        em.persist(screening1);
        em.persist(screening2);

        em.flush();
        em.clear();

        ScreeningSearchCondition condition = new ScreeningSearchCondition(
            null,
            null,
            "01011111111",
            null,
            null
        );
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<ScreeningView> screeningMembers = screeningQueryRepository.findScreenings(condition, pageRequest);

        // then
        assertThat(screeningMembers.getTotalElements()).isEqualTo(1);
        assertThat(screeningMembers.getContent().getFirst().nickname()).isEqualTo("member1");
    }

    @Test
    @DisplayName("시작일과 종료일을 설정해 심사를 조회합니다.")
    void findScreeningsByStartDateAndEndDate() {
        // given
        Member member1 = createMember("member1", "01011111111");
        Member member2 = createMember("member2", "01022222222");
        em.persist(member1);
        em.persist(member2);

        Screening screening1 = Screening.from(member1.getId());
        Screening screening2 = Screening.from(member2.getId());
        em.persist(screening1);
        em.persist(screening2);

        em.flush();
        em.clear();

        ScreeningSearchCondition condition = new ScreeningSearchCondition(
            null,
            null,
            null,
            LocalDate.EPOCH,
            LocalDate.now().plusDays(1)
        );
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<ScreeningView> screeningMembers = screeningQueryRepository.findScreenings(condition, pageRequest);

        // then
        assertThat(screeningMembers.getTotalElements()).isEqualTo(2);
        assertThat(screeningMembers.getContent()).extracting("nickname")
            .containsExactlyInAnyOrder("member1", "member2");
    }

    @Test
    @DisplayName("모든 조건을 설정해 심사를 조회합니다.")
    void findScreenings() {
        // given
        Member member1 = createMember("member1", "01011111111");
        Member member2 = createMember("member2", "01022222222");
        em.persist(member1);
        em.persist(member2);

        Screening screening1 = Screening.from(member1.getId());
        Screening screening2 = Screening.from(member2.getId());
        em.persist(screening1);
        em.persist(screening2);

        em.flush();
        em.clear();

        ScreeningSearchCondition condition = new ScreeningSearchCondition(
            "PENDING",
            "member1",
            "01011111111",
            LocalDate.EPOCH,
            LocalDate.now().plusDays(1)
        );
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<ScreeningView> screeningMembers = screeningQueryRepository.findScreenings(condition, pageRequest);

        // then
        assertThat(screeningMembers.getTotalElements()).isEqualTo(1);
        assertThat(screeningMembers.getContent().getFirst().screeningStatus()).isEqualTo("PENDING");
        assertThat(screeningMembers.getContent().getFirst().nickname()).isEqualTo("member1");
        assertThat(screeningMembers.getContent().getFirst().gender()).isEqualTo("MALE");
        assertThat(screeningMembers.getContent().getFirst().rejectionReason()).isNull();
    }

    private Member createMember(String nickname, String phoneNumber) {
        return Member.builder()
            .phoneNumber(PhoneNumber.from(phoneNumber))
            .profile(
                MemberProfile.builder()
                    .nickname(Nickname.from(nickname))
                    .gender(Gender.MALE)
                    .build()
            )
            .build();
    }
}
